package com.boulder.boulder.services.importacion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;

/**
 * Un renglón de la plantilla, leído por nombre de columna y no por posición.
 *
 * <p>
 * Leer por nombre es lo que permite que el usuario agregue una columna suya al
 * final, o reordene las que no le sirven, sin romper la carga. Lo único que no
 * puede tocar es el texto de la cabecera.
 * </p>
 *
 * <p>
 * Ningún captador lanza excepción: cuando un valor no se puede interpretar se
 * anota el motivo y se sigue leyendo el resto del renglón. Así el usuario ve
 * los cuatro problemas que tiene una fila de una sola vez, en lugar de
 * corregir uno, volver a subir, y encontrarse el siguiente.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
public class Fila {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter LATINO = DateTimeFormatter.ofPattern("d/M/yyyy");

    private final Row fila;
    private final Map<String, Integer> columnas;
    private final Catalogos catalogos;
    private final List<String> errores = new ArrayList<>();

    private String motivoOmision;
    private String referencia = "";

    public Fila(Row fila, Map<String, Integer> columnas, Catalogos catalogos) {
        this.fila = fila;
        this.columnas = columnas;
        this.catalogos = catalogos;
    }

    /** Número de fila tal como lo muestra Excel. */
    public int numero() {
        return fila.getRowNum() + 1;
    }

    public boolean hayErrores() {
        return !errores.isEmpty();
    }

    public String motivos() {
        return String.join("; ", errores);
    }

    public void error(String mensaje) {
        errores.add(mensaje);
    }

    /** Marca el renglón como ya cargado; no es un error. */
    public void omitir(String motivo) {
        this.motivoOmision = motivo;
    }

    public boolean omitida() {
        return motivoOmision != null;
    }

    /** Texto corto que identifica el renglón en el informe de errores. */
    public String referencia() {
        return referencia;
    }

    public void referencia(String r) {
        this.referencia = r;
    }

    /** {@code true} si todas las celdas del renglón están vacías. */
    public boolean vacia() {
        for (Integer i : columnas.values()) {
            if (crudo(i) != null) {
                return false;
            }
        }
        return true;
    }

    // ---------------------------------------------------------------- texto

    public String texto(String columna) {
        return crudo(indice(columna));
    }

    /**
     * Igual que {@link #texto(String)} pero controlando el largo. Sin esto una
     * celda de más de la cuenta revienta en el INSERT y se lleva puesta la
     * transacción entera; acá solo se cae ese renglón.
     */
    public String texto(String columna, int maximo) {
        String v = texto(columna);
        if (v != null && v.length() > maximo) {
            error(columna + ": no puede pasar de " + maximo + " caracteres (tiene " + v.length() + ")");
            return null;
        }
        return v;
    }

    public String textoReq(String columna, int maximo) {
        if (falta(columna)) {
            return null;
        }
        String v = texto(columna, maximo);
        if (v == null && !ultimoErrorDe(columna)) {
            error(columna + ": obligatorio");
        }
        return v;
    }

    /** Valida contra un vocabulario cerrado, el mismo que tiene el CHECK. */
    public String opcion(String columna, boolean obligatorio, String... validos) {
        if (obligatorio && falta(columna)) {
            return null;
        }
        String v = texto(columna);
        if (v == null) {
            if (obligatorio) {
                error(columna + ": obligatorio (" + String.join(" | ", validos) + ")");
            }
            return null;
        }
        for (String ok : validos) {
            if (ok.equalsIgnoreCase(v)) {
                return ok;
            }
        }
        error(columna + ": «" + v + "» no es un valor válido (" + String.join(" | ", validos) + ")");
        return null;
    }

    // -------------------------------------------------------------- números

    public BigDecimal decimal(String columna) {
        String v = crudo(indice(columna));
        if (v == null) {
            return null;
        }
        try {
            // La coma decimal es frecuente en teclados en español y Excel la
            // deja pasar cuando la celda quedó como texto.
            return new BigDecimal(v.replace(",", ".").replace(" ", ""));
        } catch (NumberFormatException e) {
            error(columna + ": «" + v + "» no es un número");
            return null;
        }
    }

    public BigDecimal decimalReq(String columna) {
        if (falta(columna)) {
            return null;
        }
        BigDecimal v = decimal(columna);
        if (v == null && !ultimoErrorDe(columna)) {
            error(columna + ": obligatorio");
        }
        return v;
    }

    public Double doble(String columna) {
        BigDecimal v = decimal(columna);
        return v == null ? null : v.doubleValue();
    }

    public Double dobleReq(String columna) {
        BigDecimal v = decimalReq(columna);
        return v == null ? null : v.doubleValue();
    }

    public Integer entero(String columna) {
        BigDecimal v = decimal(columna);
        if (v == null) {
            return null;
        }
        try {
            return v.intValueExact();
        } catch (ArithmeticException e) {
            error(columna + ": «" + v.toPlainString() + "» tiene que ser un entero");
            return null;
        }
    }

    public Short corto(String columna) {
        Integer v = entero(columna);
        return v == null ? null : v.shortValue();
    }

    // --------------------------------------------------------------- fechas

    public LocalDate fecha(String columna) {
        Integer i = indice(columna);
        if (i == null) {
            return null;
        }
        Cell celda = fila.getCell(i);
        if (celda == null) {
            return null;
        }
        // Una celda con formato de fecha llega como numero de serie de Excel;
        // el mismo dato escrito a mano llega como texto.
        if (celda.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(celda)) {
            return celda.getLocalDateTimeCellValue().toLocalDate();
        }
        String v = crudo(i);
        if (v == null) {
            return null;
        }
        for (DateTimeFormatter f : new DateTimeFormatter[] { ISO, LATINO }) {
            try {
                return LocalDate.parse(v, f);
            } catch (java.time.format.DateTimeParseException ignorada) {
                // se prueba el siguiente formato
            }
        }
        error(columna + ": «" + v + "» no es una fecha (usa AAAA-MM-DD)");
        return null;
    }

    public LocalDate fechaReq(String columna) {
        if (falta(columna)) {
            return null;
        }
        LocalDate v = fecha(columna);
        if (v == null && !ultimoErrorDe(columna)) {
            error(columna + ": obligatorio");
        }
        return v;
    }

    // ------------------------------------------------------------ catálogos

    /**
     * Resuelve el sondaje y deja el renglón rechazado si el código no existe.
     * Es la validación que da sentido a todo lo demás: un parte que no se puede
     * colgar de un sondaje no sirve para medir ningún frente.
     */
    public UUID sondajeReq(String columna) {
        String codigo = textoReq(columna, 30);
        if (codigo == null) {
            return null;
        }
        UUID id = catalogos.sondaje(codigo);
        if (id == null) {
            error(columna + ": el sondaje «" + codigo + "» no está en el catálogo");
        }
        return id;
    }

    /**
     * Igual que {@link #sondajeReq(String)} pero admitiendo el código
     * reservado {@code VARIOS}, que usan los lotes de estufa donde se secan
     * muestras de varios sondajes juntas.
     */
    public UUID sondajeOVarios(String columna) {
        String codigo = textoReq(columna, 30);
        if (codigo == null || Catalogos.SIN_SONDAJE.equalsIgnoreCase(codigo)) {
            return null;
        }
        UUID id = catalogos.sondaje(codigo);
        if (id == null) {
            error(columna + ": el sondaje «" + codigo + "» no está en el catálogo"
                    + " (si el lote no es de un sondaje, poné " + Catalogos.SIN_SONDAJE + ")");
        }
        return id;
    }

    public UUID maquina(String columna) {
        String codigo = texto(columna, 20);
        if (codigo == null) {
            return null;
        }
        UUID id = catalogos.maquina(codigo);
        if (id == null) {
            error(columna + ": la máquina «" + codigo + "» no está en el catálogo");
        }
        return id;
    }

    public UUID plataforma(String columna) {
        String codigo = texto(columna, 20);
        if (codigo == null) {
            return null;
        }
        UUID id = catalogos.plataforma(codigo);
        if (id == null) {
            error(columna + ": la plataforma «" + codigo + "» no está en el catálogo");
        }
        return id;
    }

    /** Resuelve contra un diccionario de geología. No admite códigos sueltos. */
    public UUID geologia(String columna, String tabla, boolean obligatorio) {
        if (obligatorio && falta(columna)) {
            return null;
        }
        String codigo = texto(columna, 20);
        if (codigo == null) {
            if (obligatorio) {
                error(columna + ": obligatorio");
            }
            return null;
        }
        UUID id = catalogos.geologia(tabla, codigo);
        if (id == null) {
            error(columna + ": «" + codigo + "» no está en geologia." + tabla);
        }
        return id;
    }

    /** Busca a una persona por nombre completo; no falla si no está dada de alta. */
    public UUID empleado(String nombre) {
        return catalogos.empleado(nombre);
    }

    // --------------------------------------------------------------- lectura

    private Integer indice(String columna) {
        return columnas.get(columna);
    }

    /**
     * Anota que falta una columna que hacía falta de verdad.
     *
     * <p>
     * Solo lo comprueban los captadores obligatorios. Que la plantilla no traiga
     * una columna opcional es normal —el reporte de almacén no siempre llena la
     * ruta de las fotos— y quejarse de eso llenaría el informe de ruido que
     * tapa los errores que sí importan.
     * </p>
     */
    private boolean falta(String columna) {
        if (columnas.containsKey(columna)) {
            return false;
        }
        String aviso = "falta la columna " + columna;
        if (!errores.contains(aviso)) {
            error(aviso);
        }
        return true;
    }

    private boolean ultimoErrorDe(String columna) {
        return !errores.isEmpty() && errores.get(errores.size() - 1).startsWith(columna + ":");
    }

    /**
     * Devuelve el contenido de la celda como texto ya recortado, o {@code null}
     * si está vacía. Un número entero llega de Excel como {@code 8.0}; se le
     * saca la cola para que el código de una máquina no termine guardado como
     * «400.0».
     */
    private String crudo(Integer indice) {
        if (indice == null) {
            return null;
        }
        Cell celda = fila.getCell(indice);
        if (celda == null) {
            return null;
        }
        String v;
        switch (celda.getCellType()) {
            case STRING -> v = celda.getStringCellValue();
            case BOOLEAN -> v = String.valueOf(celda.getBooleanCellValue());
            case FORMULA -> v = porFormula(celda);
            case NUMERIC -> v = DateUtil.isCellDateFormatted(celda)
                    ? celda.getLocalDateTimeCellValue().toLocalDate().toString()
                    : sinCola(celda.getNumericCellValue());
            default -> v = null;
        }
        if (v == null) {
            return null;
        }
        v = v.trim();
        return v.isEmpty() ? null : v;
    }

    private String porFormula(Cell celda) {
        try {
            return switch (celda.getCachedFormulaResultType()) {
                case STRING -> celda.getStringCellValue();
                case NUMERIC -> sinCola(celda.getNumericCellValue());
                case BOOLEAN -> String.valueOf(celda.getBooleanCellValue());
                default -> null;
            };
        } catch (IllegalStateException e) {
            return null;
        }
    }

    private static String sinCola(double d) {
        if (d == Math.rint(d) && !Double.isInfinite(d) && Math.abs(d) < 1e15) {
            return String.valueOf((long) d);
        }
        return BigDecimal.valueOf(d).stripTrailingZeros().toPlainString();
    }

    /** Parte una celda con varios nombres separados por punto y coma o coma. */
    public List<String> lista(String columna) {
        String v = texto(columna);
        if (v == null) {
            return List.of();
        }
        return Arrays.stream(v.split("[;,\n]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
