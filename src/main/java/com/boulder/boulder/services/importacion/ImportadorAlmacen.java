package com.boulder.boulder.services.importacion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boulder.boulder.dto.importacion.ResultadoImportacionDTO;
import com.boulder.boulder.entities.AvanceDensidad;
import com.boulder.boulder.entities.AvanceFotografia;
import com.boulder.boulder.entities.AvanceMuestreo;
import com.boulder.boulder.entities.AvanceOperativo;
import com.boulder.boulder.entities.CorteDensidad;
import com.boulder.boulder.entities.CorteMuestra;
import com.boulder.boulder.entities.RecepcionCaja;
import com.boulder.boulder.entities.Sondaje;
import com.boulder.boulder.repositories.AvanceDensidadRepository;
import com.boulder.boulder.repositories.AvanceFotografiaRepository;
import com.boulder.boulder.repositories.AvanceMuestreoRepository;
import com.boulder.boulder.repositories.CorteDensidadRepository;
import com.boulder.boulder.repositories.CorteMuestraRepository;
import com.boulder.boulder.repositories.RecepcionCajaRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Carga el reporte diario de almacén de testigos: las seis etapas que ocurren
 * entre que la caja llega del sondaje y la muestra sale al laboratorio
 * (CU12 a CU17).
 *
 * <p>
 * Van en un solo libro con seis hojas porque así se reporta: el turno de
 * almacén cierra un parte único donde recepción, fotografía, corte, densidad,
 * estufa y muestreo son columnas de la misma planilla. Partirlo en seis
 * archivos obligaría a subir seis veces lo que se llena de una sentada.
 * </p>
 *
 * <p>
 * La hoja de estufa admite el código {@code VARIOS}: un lote de secado mezcla
 * muestras de varios sondajes y no cuelga de ninguno. Es la única que lo
 * admite, y por eso el sondaje entra en la clave natural como código y no
 * solo como identificador.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Component
public class ImportadorAlmacen extends ImportadorBase {

    @Autowired
    private RecepcionCajaRepository recepciones;
    @Autowired
    private AvanceFotografiaRepository fotografias;
    @Autowired
    private CorteMuestraRepository cortes;
    @Autowired
    private CorteDensidadRepository cortesDensidad;
    @Autowired
    private AvanceDensidadRepository estufas;
    @Autowired
    private AvanceMuestreoRepository muestreos;

    @PersistenceContext
    private EntityManager em;

    @Override
    public String clave() {
        return "almacen";
    }

    @Override
    public String archivoPlantilla() {
        return "PLANTILLA_reporte_almacen.xlsx";
    }

    @Override
    public String descripcion() {
        return "Reporte de almacén: recepción de cajas, fotografía, corte, densidad, estufa y muestreo.";
    }

    @Override
    protected void procesar(Workbook libro, Catalogos catalogos, boolean aplicar,
            ResultadoImportacionDTO resultado) {
        recepcionCajas(libro, catalogos, aplicar, resultado);
        fotografia(libro, catalogos, aplicar, resultado);
        corteMuestras(libro, catalogos, aplicar, resultado);
        corteDensidad(libro, catalogos, aplicar, resultado);
        estufaDensidad(libro, catalogos, aplicar, resultado);
        muestreo(libro, catalogos, aplicar, resultado);
    }

    /** Cabecera común a las seis hojas: sondaje, fecha y turno. */
    private UUID cabecera(Fila f, AvanceOperativo e) {
        UUID sondajeId = f.sondajeOVarios("sondaje_codigo");
        e.setSondajeCodigo(f.texto("sondaje_codigo", 30));
        e.setFecha(f.fechaReq("fecha"));
        e.setTurno(f.opcion("turno", false, "dia", "noche"));
        e.setObservaciones(f.texto("observaciones"));
        e.setEstado("registrado");
        f.referencia(f.texto("sondaje_codigo") + " " + f.texto("fecha"));
        return sondajeId;
    }

    private void vincular(AvanceOperativo e, UUID sondajeId) {
        if (sondajeId != null) {
            e.setSondaje(em.getReference(Sondaje.class, sondajeId));
        }
    }

    private void validarTramo(Fila f, BigDecimal desde, BigDecimal hasta) {
        if (desde != null && hasta != null && hasta.compareTo(desde) < 0) {
            f.error("hasta no puede ser menor que desde");
        }
    }

    private void recepcionCajas(Workbook libro, Catalogos cat, boolean aplicar, ResultadoImportacionDTO r) {
        String sql = "SELECT " + sqlIdOCodigo() + " || '|' || " + sqlTexto("fecha") + " || '|' || "
                + sqlTexto("turno") + " || '|' || " + sqlTexto("box_desde") + " || '|' || "
                + sqlNum("marcado_desde") + " FROM operacional.log_recepcion_caja";

        hoja(libro, "RECEPCION_CAJAS", "operacional.log_recepcion_caja", sql, cat, aplicar, r, f -> {
            RecepcionCaja e = new RecepcionCaja();
            UUID sondajeId = cabecera(f, e);
            e.setResponsable(f.texto("responsable", 80));
            e.setBoxDesde(f.entero("box_desde"));
            e.setBoxHasta(f.entero("box_hasta"));
            e.setTotalCajas(f.entero("total_cajas"));
            e.setMarcadoDesde(f.decimal("marcado_desde"));
            e.setMarcadoHasta(f.decimal("marcado_hasta"));
            e.setTotalMetraje(f.decimal("total_metraje"));

            validarTramo(f, e.getMarcadoDesde(), e.getMarcadoHasta());
            if (e.getBoxDesde() != null && e.getBoxHasta() != null && e.getBoxHasta() < e.getBoxDesde()) {
                f.error("box_hasta no puede ser menor que box_desde");
            }
            if (e.getTotalCajas() != null && e.getTotalCajas() < 0) {
                f.error("total_cajas no puede ser negativo");
            }
            if (f.hayErrores()) {
                return null;
            }
            vincular(e, sondajeId);
            return new Registro(
                    clave(idOCodigo(sondajeId, e.getSondajeCodigo()), e.getFecha(), e.getTurno(),
                            e.getBoxDesde(), e.getMarcadoDesde()),
                    () -> recepciones.saveAndFlush(e));
        });
    }

    private void fotografia(Workbook libro, Catalogos cat, boolean aplicar, ResultadoImportacionDTO r) {
        String sql = "SELECT " + sqlIdOCodigo() + " || '|' || " + sqlTexto("fecha") + " || '|' || "
                + sqlTexto("turno") + " || '|' || " + sqlTexto("box_desde") + " || '|' || "
                + sqlNum("marcado_desde") + " FROM operacional.fot_avance";

        hoja(libro, "FOTOGRAFIA", "operacional.fot_avance", sql, cat, aplicar, r, f -> {
            AvanceFotografia e = new AvanceFotografia();
            UUID sondajeId = cabecera(f, e);
            e.setResponsable(f.texto("responsable", 80));
            e.setBoxDesde(f.entero("box_desde"));
            e.setBoxHasta(f.entero("box_hasta"));
            e.setTotalCajas(f.entero("total_cajas"));
            e.setMarcadoDesde(f.decimal("marcado_desde"));
            e.setMarcadoHasta(f.decimal("marcado_hasta"));
            e.setTotalMetraje(f.decimal("total_metraje"));
            e.setSecoHumedo(f.opcion("seco_humedo", false, "seco", "humedo"));
            e.setNroFotos(f.entero("nro_fotos"));
            e.setRutaArchivos(f.texto("ruta_archivos"));

            validarTramo(f, e.getMarcadoDesde(), e.getMarcadoHasta());
            if (f.hayErrores()) {
                return null;
            }
            vincular(e, sondajeId);
            return new Registro(
                    clave(idOCodigo(sondajeId, e.getSondajeCodigo()), e.getFecha(), e.getTurno(),
                            e.getBoxDesde(), e.getMarcadoDesde()),
                    () -> fotografias.saveAndFlush(e));
        });
    }

    private void corteMuestras(Workbook libro, Catalogos cat, boolean aplicar, ResultadoImportacionDTO r) {
        String sql = "SELECT " + sqlIdOCodigo() + " || '|' || " + sqlTexto("fecha") + " || '|' || "
                + sqlTexto("turno") + " || '|' || " + sqlNum("desde") + " FROM operacional.crt_muestra";

        hoja(libro, "CORTE_MUESTRAS", "operacional.crt_muestra", sql, cat, aplicar, r, f -> {
            CorteMuestra e = new CorteMuestra();
            UUID sondajeId = cabecera(f, e);
            e.setResponsable(f.texto("responsable", 80));
            e.setDesde(f.decimalReq("desde"));
            e.setHasta(f.decimalReq("hasta"));
            e.setTipoLinea(f.texto("tipo_linea", 10));
            e.setMetrajeEstimado(f.decimal("metraje_estimado"));
            e.setNroMuestras(f.entero("nro_muestras"));

            validarTramo(f, e.getDesde(), e.getHasta());
            if (f.hayErrores()) {
                return null;
            }
            vincular(e, sondajeId);
            return new Registro(
                    clave(idOCodigo(sondajeId, e.getSondajeCodigo()), e.getFecha(), e.getTurno(), e.getDesde()),
                    () -> cortes.saveAndFlush(e));
        });
    }

    private void corteDensidad(Workbook libro, Catalogos cat, boolean aplicar, ResultadoImportacionDTO r) {
        String sql = "SELECT " + sqlIdOCodigo() + " || '|' || " + sqlTexto("fecha") + " || '|' || "
                + sqlTexto("turno") + " || '|' || " + sqlNum("desde") + " FROM operacional.crt_densidad";

        hoja(libro, "CORTE_DENSIDAD", "operacional.crt_densidad", sql, cat, aplicar, r, f -> {
            CorteDensidad e = new CorteDensidad();
            UUID sondajeId = cabecera(f, e);
            e.setResponsable(f.texto("responsable", 80));
            e.setDesde(f.decimalReq("desde"));
            e.setHasta(f.decimalReq("hasta"));
            e.setTotalMuestras(f.entero("total_muestras"));
            e.setTotalMetraje(f.decimal("total_metraje"));
            e.setTipoLinea(f.texto("tipo_linea", 10));

            validarTramo(f, e.getDesde(), e.getHasta());
            if (e.getTotalMuestras() == null) {
                f.error("total_muestras: obligatorio");
            } else if (e.getTotalMuestras() < 0) {
                f.error("total_muestras no puede ser negativo");
            }
            if (f.hayErrores()) {
                return null;
            }
            vincular(e, sondajeId);
            return new Registro(
                    clave(idOCodigo(sondajeId, e.getSondajeCodigo()), e.getFecha(), e.getTurno(), e.getDesde()),
                    () -> cortesDensidad.saveAndFlush(e));
        });
    }

    private void estufaDensidad(Workbook libro, Catalogos cat, boolean aplicar, ResultadoImportacionDTO r) {
        String sql = "SELECT " + sqlIdOCodigo() + " || '|' || " + sqlTexto("fecha") + " || '|' || "
                + sqlTexto("turno") + " || '|' || " + sqlTexto("etapa") + " FROM operacional.dns_avance";

        hoja(libro, "ESTUFA_DENSIDAD", "operacional.dns_avance", sql, cat, aplicar, r, f -> {
            AvanceDensidad e = new AvanceDensidad();
            UUID sondajeId = cabecera(f, e);
            e.setResponsable(f.texto("responsable", 80));
            e.setEtapa(f.opcion("etapa", true, "corte", "secado_1", "secado_2", "pesado", "calculo"));
            e.setNroMuestras(f.entero("nro_muestras"));
            e.setTemperaturaC(f.decimal("temperatura_c"));
            e.setTipoLinea(f.texto("tipo_linea", 10));
            e.setDesde(f.decimal("desde"));
            e.setHasta(f.decimal("hasta"));

            validarTramo(f, e.getDesde(), e.getHasta());
            if (e.getNroMuestras() == null) {
                f.error("nro_muestras: obligatorio");
            } else if (e.getNroMuestras() < 0) {
                f.error("nro_muestras no puede ser negativo");
            }
            if (f.hayErrores()) {
                return null;
            }
            vincular(e, sondajeId);
            return new Registro(
                    clave(idOCodigo(sondajeId, e.getSondajeCodigo()), e.getFecha(), e.getTurno(), e.getEtapa()),
                    () -> estufas.saveAndFlush(e));
        });
    }

    private void muestreo(Workbook libro, Catalogos cat, boolean aplicar, ResultadoImportacionDTO r) {
        String sql = "SELECT " + sqlIdOCodigo() + " || '|' || " + sqlTexto("fecha") + " || '|' || "
                + sqlTexto("turno") + " || '|' || " + sqlTexto("estado_muestreo") + " || '|' || "
                + sqlNum("desde") + " FROM operacional.mst_avance";

        hoja(libro, "MUESTREO", "operacional.mst_avance", sql, cat, aplicar, r, f -> {
            AvanceMuestreo e = new AvanceMuestreo();
            UUID sondajeId = cabecera(f, e);
            e.setResponsable(f.texto("responsable", 80));
            e.setEstadoMuestreo(f.opcion("estado_muestreo", true, "en_proceso", "finalizado"));
            e.setDesde(f.decimal("desde"));
            e.setHasta(f.decimal("hasta"));
            e.setAvance(f.decimal("avance"));
            e.setNroMuestras(f.entero("nro_muestras"));
            e.setNroMuestrasEmbolsadas(f.entero("nro_muestras_embolsadas"));
            e.setNroCostales(f.entero("nro_costales"));
            e.setNroLotes(f.entero("nro_lotes"));

            validarTramo(f, e.getDesde(), e.getHasta());
            if (f.hayErrores()) {
                return null;
            }
            vincular(e, sondajeId);
            return new Registro(
                    clave(idOCodigo(sondajeId, e.getSondajeCodigo()), e.getFecha(), e.getTurno(),
                            e.getEstadoMuestreo(), e.getDesde()),
                    () -> muestreos.saveAndFlush(e));
        });
    }
}
