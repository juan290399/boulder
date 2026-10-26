package com.boulder.boulder.services.importacion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boulder.boulder.dto.importacion.ResultadoImportacionDTO;
import com.boulder.boulder.entities.AvanceLogueo;
import com.boulder.boulder.entities.Sondaje;
import com.boulder.boulder.repositories.AvanceLogueoRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Carga el avance de logueo (UC16).
 *
 * <p>
 * El tipo de logueo forma parte de la clave natural y no es un detalle: sobre
 * el mismo tramo de testigo avanzan el geológico, el geomecánico y el
 * estructural por separado, y los tres tienen que poder convivir en la misma
 * fecha sin pisarse.
 * </p>
 *
 * <p>
 * En la base el tipo se llama {@code geotecnico} aunque el reporte diario diga
 * «geomecánico». Se acepta escrito de las dos formas y se guarda siempre el
 * valor que admite el CHECK de la tabla, que es el que ya usa la aplicación
 * móvil.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Component
public class ImportadorLogueo extends ImportadorBase {

    @Autowired
    private AvanceLogueoRepository repositorio;

    @PersistenceContext
    private EntityManager em;

    @Override
    public String clave() {
        return "logueo";
    }

    @Override
    public String archivoPlantilla() {
        return "PLANTILLA_avance_logueo.xlsx";
    }

    @Override
    public String descripcion() {
        return "Avance de logueo geológico, geomecánico y estructural por tramo.";
    }

    @Override
    protected void procesar(Workbook libro, Catalogos catalogos, boolean aplicar,
            ResultadoImportacionDTO resultado) {

        String sqlClaves = "SELECT " + sqlTexto("sondaje_id") + " || '|' || " + sqlTexto("fecha")
                + " || '|' || " + sqlTexto("tipo_logueo") + " || '|' || " + sqlNum("desde")
                + " FROM operacional.log_avance";

        hoja(libro, "DATOS", "operacional.log_avance", sqlClaves, catalogos, aplicar, resultado, f -> {

            UUID sondajeId = f.sondajeReq("sondaje_codigo");
            LocalDate fecha = f.fechaReq("fecha");
            String tipo = tipoLogueo(f);
            BigDecimal desde = f.decimalReq("desde");
            BigDecimal hasta = f.decimalReq("hasta");
            BigDecimal avance = f.decimalReq("avance");
            BigDecimal pct = f.decimal("pct_completado");

            f.referencia(f.texto("sondaje_codigo") + " " + f.texto("fecha") + " " + (tipo == null ? "" : tipo));

            if (desde != null && hasta != null && hasta.compareTo(desde) < 0) {
                f.error("hasta no puede ser menor que desde");
            }
            if (desde != null && hasta != null && avance != null
                    && hasta.subtract(desde).compareTo(avance) != 0) {
                f.error("avance (" + avance.toPlainString() + ") no coincide con hasta menos desde ("
                        + hasta.subtract(desde).toPlainString() + ")");
            }
            if (pct != null && (pct.signum() < 0 || pct.compareTo(BigDecimal.valueOf(100)) > 0)) {
                f.error("pct_completado: tiene que estar entre 0 y 100");
            }

            AvanceLogueo e = new AvanceLogueo();
            e.setSondajeCodigo(f.texto("sondaje_codigo", 30));
            e.setFecha(fecha);
            e.setTurno(f.opcion("turno", false, "dia", "noche"));
            e.setTipoLogueo(tipo);
            e.setGeologo(f.texto("geologo", 80));
            e.setDesde(desde);
            e.setHasta(hasta);
            e.setAvance(avance);
            e.setPctCompletado(pct);
            e.setObservaciones(f.texto("observaciones"));
            e.setEstado("registrado");

            if (f.hayErrores()) {
                return null;
            }
            e.setSondaje(em.getReference(Sondaje.class, sondajeId));

            return new Registro(clave(sondajeId, fecha, tipo, desde), () -> repositorio.saveAndFlush(e));
        });
    }

    /**
     * Acepta «geomecanico» como sinónimo de «geotecnico». Quien llena la
     * plantilla copia del reporte diario, y el reporte diario usa la palabra
     * que no está en la base.
     */
    private String tipoLogueo(Fila f) {
        String v = f.texto("tipo_logueo");
        if (v != null && ("geomecanico".equalsIgnoreCase(v) || "geomecánico".equalsIgnoreCase(v))) {
            return "geotecnico";
        }
        return f.opcion("tipo_logueo", true, "geologico", "geotecnico", "estructural", "rapido");
    }
}
