package com.boulder.boulder.services.importacion;

import java.math.BigDecimal;
import java.util.UUID;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;

import com.boulder.boulder.dto.importacion.ResultadoImportacionDTO;
import com.boulder.boulder.entities.Quicklog;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Carga el quicklog: la descripción rápida del testigo tramo por tramo.
 *
 * <p>
 * Es la plantilla con más columnas y la más exigente con los catálogos. Cada
 * sigla —litología, alteración, mineral, estilo, zona— tiene que resolver
 * contra su diccionario de {@code geologia}; no se admite el código suelto. Un
 * quicklog con litologías escritas a mano no se puede cruzar con nada, y
 * cruzarlo es todo lo que se le pide.
 * </p>
 *
 * <p>
 * Las tres familias de columnas van contra diccionarios distintos y conviene
 * no confundirlas: {@code alt_tipo_*} es el mineral de alteración del catálogo
 * general, {@code alt_aso_*} es el mineral asociado —que tiene su propia lista,
 * más corta— y {@code min_estilo_*} no es un estilo de alteración sino la
 * distribución de la mineralización.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Component
public class ImportadorQuicklog extends ImportadorBase {

    @PersistenceContext
    private EntityManager em;

    @Override
    public String clave() {
        return "quicklog";
    }

    @Override
    public String archivoPlantilla() {
        return "PLANTILLA_quicklog.xlsx";
    }

    @Override
    public String descripcion() {
        return "Quicklog: litología, alteración y mineralización por tramo de testigo.";
    }

    @Override
    protected void procesar(Workbook libro, Catalogos catalogos, boolean aplicar,
            ResultadoImportacionDTO resultado) {

        String sqlClaves = "SELECT sondaje_id::text || '|' || " + sqlNum("desde")
                + " FROM logueo.iquicklog WHERE eliminado_en IS NULL";

        hoja(libro, "DATOS", "logueo.iquicklog", sqlClaves, catalogos, aplicar, resultado, f -> {

            UUID sondajeId = f.sondajeReq("sondaje_codigo");
            BigDecimal desde = f.decimalReq("desde");
            BigDecimal hasta = f.decimalReq("hasta");
            BigDecimal longitud = f.decimalReq("longitud");

            f.referencia(f.texto("sondaje_codigo") + " " + f.texto("desde") + "-" + f.texto("hasta") + " m");

            if (desde != null && hasta != null && hasta.compareTo(desde) <= 0) {
                f.error("hasta tiene que ser mayor que desde");
            }
            if (desde != null && hasta != null && longitud != null
                    && hasta.subtract(desde).compareTo(longitud) != 0) {
                f.error("longitud (" + longitud.toPlainString() + ") no coincide con hasta menos desde ("
                        + hasta.subtract(desde).toPlainString() + ")");
            }

            Quicklog e = new Quicklog();
            e.setDesde(desde);
            e.setHasta(hasta);
            e.setLongitud(longitud);
            e.setFecha(f.fecha("fecha"));
            e.setGeologo(f.texto("geologo", 100));
            e.setComentarios(f.texto("comentarios"));

            e.setLitoId(f.geologia("lito", "lit_diccionario", true));

            e.setAltTipo1Id(f.geologia("alt_tipo_1", "alt_diccionario", false));
            e.setAltTipo2Id(f.geologia("alt_tipo_2", "alt_diccionario", false));
            e.setAltIntensidad1Id(f.geologia("alt_intensidad_1", "alt_intensidad", false));
            e.setAltIntensidad2Id(f.geologia("alt_intensidad_2", "alt_intensidad", false));
            e.setAltAso1Id(f.geologia("alt_aso_1", "alt_mineral", false));
            e.setAltAso2Id(f.geologia("alt_aso_2", "alt_mineral", false));
            e.setAltAso3Id(f.geologia("alt_aso_3", "alt_mineral", false));

            e.setMinTipo1Id(f.geologia("min_tipo_1", "min_diccionario", false));
            e.setMinTipo2Id(f.geologia("min_tipo_2", "min_diccionario", false));
            e.setMinTipo3Id(f.geologia("min_tipo_3", "min_diccionario", false));
            e.setMinEstilo1Id(f.geologia("min_estilo_1", "min_distribucion", false));
            e.setMinEstilo2Id(f.geologia("min_estilo_2", "min_distribucion", false));
            e.setMinEstilo3Id(f.geologia("min_estilo_3", "min_distribucion", false));
            e.setMinZonaId(f.geologia("min_zona", "min_zona", false));

            e.setPcPy(porcentaje(f, "pc_py"));
            e.setPcCu(porcentaje(f, "pc_cu"));
            e.setPcMo(porcentaje(f, "pc_mo"));

            if (f.hayErrores()) {
                return null;
            }
            e.setSondajeId(sondajeId);
            // La tabla tiene DEFAULT gen_random_uuid(), pero Hibernate manda la
            // columna igual y el default nunca llega a aplicarse.
            e.setId(UUID.randomUUID());

            return new Registro(clave(sondajeId, desde), () -> {
                em.persist(e);
                em.flush();
            });
        });
    }

    private BigDecimal porcentaje(Fila f, String columna) {
        BigDecimal v = f.decimal(columna);
        if (v != null && (v.signum() < 0 || v.compareTo(BigDecimal.valueOf(100)) > 0)) {
            f.error(columna + ": tiene que estar entre 0 y 100");
            return null;
        }
        return v;
    }
}
