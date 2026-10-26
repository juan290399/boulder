package com.boulder.boulder.services.importacion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.boulder.boulder.dto.importacion.ErrorFilaDTO;
import com.boulder.boulder.dto.importacion.ResultadoHojaDTO;
import com.boulder.boulder.dto.importacion.ResultadoImportacionDTO;

/**
 * Maquinaria común a todos los importadores: recorrer una hoja, ubicar las
 * columnas por su nombre, descartar los renglones en blanco, contar lo que
 * entró y anotar lo que no.
 *
 * <p>
 * El renglón se escribe una sola vez aunque la plantilla se suba diez veces.
 * Eso lo resuelve la clave natural: el conjunto de columnas que identifica al
 * hecho —sondaje, fecha, turno y tramo, según la tabla— y no un identificador
 * que el usuario no tiene. Sin esto el primer reintento dejaría la base al
 * doble, que es exactamente el problema que esta plataforma viene a resolver.
 * </p>
 *
 * <p>
 * La clave se compara contra dos conjuntos: lo que ya está en la base, leído de
 * una sola consulta al empezar la hoja, y lo que ya apareció en este mismo
 * archivo. El segundo importa tanto como el primero: una plantilla que repite
 * el mismo parte dos veces es un error del que la llenó, y conviene que se
 * entere antes de cargar.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
public abstract class ImportadorBase implements ImportadorPlantilla {

    private static final Logger log = LoggerFactory.getLogger(ImportadorBase.class);

    @Autowired
    protected JdbcTemplate jdbc;

    /** Lo que el manejador devuelve por cada renglón que interpretó bien. */
    protected record Registro(String clave, Runnable grabar) {
    }

    /**
     * Interpreta un renglón. Devuelve {@code null} —o deja errores anotados en
     * la fila— cuando el renglón no se puede cargar.
     */
    @FunctionalInterface
    protected interface ManejadorFila {
        Registro procesar(Fila fila);
    }

    @Override
    public ResultadoImportacionDTO importar(Workbook libro, Catalogos catalogos, boolean aplicar) {
        ResultadoImportacionDTO resultado = new ResultadoImportacionDTO();
        resultado.setPlantilla(clave());
        resultado.setAplicado(aplicar);
        procesar(libro, catalogos, aplicar, resultado);
        resultado.totalizar();
        return resultado;
    }

    /** Cada plantilla declara acá sus hojas. */
    protected abstract void procesar(Workbook libro, Catalogos catalogos, boolean aplicar,
            ResultadoImportacionDTO resultado);

    /**
     * Recorre una hoja de principio a fin.
     *
     * @param sqlClaves consulta que devuelve una columna de texto con las claves
     *                  naturales que ya están en la base, armadas igual que las
     *                  que produce el manejador
     */
    protected void hoja(Workbook libro, String nombre, String destino, String sqlClaves,
            Catalogos catalogos, boolean aplicar, ResultadoImportacionDTO resultado,
            ManejadorFila manejador) {

        ResultadoHojaDTO r = new ResultadoHojaDTO();
        r.setHoja(nombre);
        r.setDestino(destino);
        resultado.getHojas().add(r);

        Sheet hoja = libro.getSheet(nombre);
        if (hoja == null) {
            r.setAdvertencia("El libro no trae la hoja " + nombre + ". No se cargó nada de ella.");
            return;
        }

        Map<String, Integer> columnas = cabecera(hoja);
        if (columnas.isEmpty()) {
            r.setAdvertencia("La hoja " + nombre + " no tiene fila de cabecera.");
            return;
        }

        Set<String> enLaBase = new HashSet<>(jdbc.queryForList(sqlClaves, String.class));
        Set<String> enElArchivo = new HashSet<>();

        for (int i = 1; i <= hoja.getLastRowNum(); i++) {
            Row cruda = hoja.getRow(i);
            if (cruda == null) {
                continue;
            }
            Fila fila = new Fila(cruda, columnas, catalogos);
            if (fila.vacia()) {
                continue;
            }
            r.setLeidas(r.getLeidas() + 1);

            Registro reg = interpretar(fila, manejador);

            if (fila.hayErrores() || reg == null) {
                r.setRechazadas(r.getRechazadas() + 1);
                r.getErrores().add(new ErrorFilaDTO(fila.numero(), fila.referencia(),
                        fila.hayErrores() ? fila.motivos() : "no se pudo interpretar el renglón"));
                continue;
            }
            if (!enElArchivo.add(reg.clave())) {
                r.setOmitidas(r.getOmitidas() + 1);
                continue;
            }
            if (enLaBase.contains(reg.clave())) {
                r.setOmitidas(r.getOmitidas() + 1);
                continue;
            }
            if (aplicar) {
                try {
                    reg.grabar().run();
                } catch (RuntimeException e) {
                    throw new ImportacionAbortadaException(nombre, fila.numero(), raiz(e), e);
                }
            }
            r.setAceptadas(r.getAceptadas() + 1);
        }

        log.info("Hoja {}: {} leídas, {} aceptadas, {} omitidas, {} rechazadas (aplicar={})",
                nombre, r.getLeidas(), r.getAceptadas(), r.getOmitidas(), r.getRechazadas(), aplicar);
    }

    /**
     * Un fallo al interpretar el renglón no puede llevarse la importación
     * entera: se anota contra esa fila y se sigue con la siguiente.
     */
    private Registro interpretar(Fila fila, ManejadorFila manejador) {
        try {
            return manejador.procesar(fila);
        } catch (ImportacionAbortadaException e) {
            throw e;
        } catch (RuntimeException e) {
            log.warn("Fila {} ilegible", fila.numero(), e);
            fila.error("no se pudo leer: " + raiz(e));
            return null;
        }
    }

    /**
     * Ubica las columnas por el texto de la cabecera. Leer por nombre y no por
     * posición es lo que permite que la plantilla se le agregue una columna o
     * se le reordenen las que no se usan sin romper la carga.
     */
    private Map<String, Integer> cabecera(Sheet hoja) {
        Map<String, Integer> columnas = new HashMap<>();
        Row fila = hoja.getRow(0);
        if (fila == null) {
            return columnas;
        }
        for (int i = 0; i < fila.getLastCellNum(); i++) {
            Cell celda = fila.getCell(i);
            if (celda == null) {
                continue;
            }
            String nombre = celda.toString().trim().toLowerCase();
            if (!nombre.isEmpty()) {
                columnas.putIfAbsent(nombre, i);
            }
        }
        return columnas;
    }

    /**
     * Arma la clave natural. Los nulos entran como cadena vacía para que la
     * ausencia de turno sea un valor más y no borre la separación entre partes.
     */
    protected static String clave(Object... partes) {
        StringBuilder sb = new StringBuilder();
        for (Object p : partes) {
            if (sb.length() > 0) {
                sb.append('|');
            }
            sb.append(p == null ? "" : texto(p));
        }
        return sb.toString();
    }

    private static String texto(Object p) {
        if (p instanceof BigDecimal d) {
            return num(d);
        }
        if (p instanceof Double d) {
            return num(BigDecimal.valueOf(d));
        }
        return p.toString();
    }

    /**
     * Normaliza un número para la clave. Tiene que dar el mismo texto que la
     * consulta que lee las claves ya cargadas: {@code 134}, {@code 134.0} y
     * {@code 134.00} son la misma profundidad y deben producir la misma clave,
     * o la segunda carga duplicaría todo.
     */
    protected static String num(BigDecimal v) {
        return v == null ? "" : v.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    /** Expresión SQL equivalente a {@link #num(BigDecimal)}. */
    protected static String sqlNum(String columna) {
        return "coalesce(trim(to_char(" + columna + ", 'FM9999999990.00')), '')";
    }

    /**
     * Parte de la clave que identifica al sondaje. Los partes de estufa pueden
     * no tener sondaje —un lote seca muestras de varios a la vez— y entonces lo
     * que identifica al renglón es el código literal {@code VARIOS}.
     */
    protected static Object idOCodigo(UUID sondajeId, String codigo) {
        return sondajeId != null ? sondajeId : (codigo == null ? "" : codigo.toUpperCase());
    }

    /** Expresión SQL equivalente a {@link #idOCodigo(UUID, String)}. */
    protected static String sqlIdOCodigo() {
        return "coalesce(sondaje_id::text, upper(sondaje_codigo), '')";
    }

    /** Expresión SQL para una columna de texto que puede venir nula. */
    protected static String sqlTexto(String columna) {
        return "coalesce(" + columna + "::text, '')";
    }

    private static String raiz(Throwable e) {
        Throwable t = e;
        while (t.getCause() != null && t.getCause() != t) {
            t = t.getCause();
        }
        String m = t.getMessage();
        if (m == null) {
            return t.getClass().getSimpleName();
        }
        m = m.replaceAll("\s+", " ").trim();
        return m.length() > 300 ? m.substring(0, 300) + "…" : m;
    }
}
