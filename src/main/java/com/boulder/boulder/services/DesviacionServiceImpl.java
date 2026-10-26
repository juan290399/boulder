package com.boulder.boulder.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boulder.boulder.dto.DesviacionMaquinaDTO;
import com.boulder.boulder.dto.DesviacionPerforistaDTO;
import com.boulder.boulder.dto.DesviacionSondajeDTO;
import com.boulder.boulder.dto.EstacionSurveyDTO;
import com.boulder.boulder.dto.TrayectoriaSondajeDTO;
import com.boulder.boulder.entities.Sondaje;
import com.boulder.boulder.entities.Survey;
import com.boulder.boulder.repositories.SondajeRepository;
import com.boulder.boulder.repositories.SurveyRepository;

import org.springframework.beans.factory.annotation.Autowired;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;

/**
 * Implementación de las consultas de analítica de desviaciones sobre las vistas
 * {@code operacional.vw_desviacion_*} mediante consultas nativas.
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Service
public class DesviacionServiceImpl implements DesviacionService {

    private static final Logger log = LoggerFactory.getLogger(DesviacionServiceImpl.class);

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private SondajeRepository sondajeRepository;

    @Autowired
    private SurveyRepository surveyRepository;

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<DesviacionPerforistaDTO> resumenPorPerforista() {
        log.debug("Consultando vw_desviacion_por_perforista");
        List<Object[]> filas = em.createNativeQuery(
                "SELECT perforista_id, perforista_codigo, perforista, n_mediciones, "
              + "desv_az_prom_abs, desv_az_max_abs, desv_dip_prom_abs, desv_dip_max_abs, "
              + "desv_metros_prom, desv_metros_max, n_correcciones "
              + "FROM operacional.vw_desviacion_por_perforista "
              + "ORDER BY desv_az_prom_abs DESC NULLS LAST").getResultList();

        List<DesviacionPerforistaDTO> out = new ArrayList<>(filas.size());
        for (Object[] r : filas) {
            DesviacionPerforistaDTO d = new DesviacionPerforistaDTO();
            d.setPerforistaId(uuid(r[0]));
            d.setPerforistaCodigo(str(r[1]));
            d.setPerforista(str(r[2]));
            d.setNMediciones(lng(r[3]));
            d.setDesvAzPromAbs(dbl(r[4]));
            d.setDesvAzMaxAbs(dbl(r[5]));
            d.setDesvDipPromAbs(dbl(r[6]));
            d.setDesvDipMaxAbs(dbl(r[7]));
            d.setDesvMetrosProm(dbl(r[8]));
            d.setDesvMetrosMax(dbl(r[9]));
            d.setNCorrecciones(lng(r[10]));
            out.add(d);
        }
        return out;
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<DesviacionMaquinaDTO> resumenPorMaquina() {
        log.debug("Consultando vw_desviacion_por_maquina");
        List<Object[]> filas = em.createNativeQuery(
                "SELECT maquina_id, maquina_codigo, maquina_nombre, n_mediciones, "
              + "desv_az_prom_abs, desv_az_max_abs, desv_dip_prom_abs, desv_dip_max_abs, "
              + "desv_metros_prom, desv_metros_max, n_correcciones "
              + "FROM operacional.vw_desviacion_por_maquina "
              + "ORDER BY desv_az_prom_abs DESC NULLS LAST").getResultList();

        List<DesviacionMaquinaDTO> out = new ArrayList<>(filas.size());
        for (Object[] r : filas) {
            DesviacionMaquinaDTO d = new DesviacionMaquinaDTO();
            d.setMaquinaId(uuid(r[0]));
            d.setMaquinaCodigo(str(r[1]));
            d.setMaquinaNombre(str(r[2]));
            d.setNMediciones(lng(r[3]));
            d.setDesvAzPromAbs(dbl(r[4]));
            d.setDesvAzMaxAbs(dbl(r[5]));
            d.setDesvDipPromAbs(dbl(r[6]));
            d.setDesvDipMaxAbs(dbl(r[7]));
            d.setDesvMetrosProm(dbl(r[8]));
            d.setDesvMetrosMax(dbl(r[9]));
            d.setNCorrecciones(lng(r[10]));
            out.add(d);
        }
        return out;
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<DesviacionSondajeDTO> evolucionPorSondaje(String sondajeCodigo) {
        log.debug("Consultando vw_desviacion_por_sondaje. Filtro sondaje: {}", sondajeCodigo);
        List<Object[]> filas = em.createNativeQuery(
                "SELECT sondaje_codigo, sondaje_nombre, profundidad, nro_medicion, fecha_medicion, "
              + "maquina_codigo, perforista, desv_azimut, desv_azimut_sentido, desv_inclinacion, "
              + "desv_inclinacion_sentido, desv_metros, desv_total, decision, "
              + "medicion_id, sondaje_id, interpretacion, instruccion, "
              + "responsable_seguimiento, estado_seguimiento "
              + "FROM operacional.vw_desviacion_por_sondaje "
              + "WHERE (CAST(:cod AS text) IS NULL OR sondaje_codigo = CAST(:cod AS text)) "
              + "ORDER BY sondaje_codigo, profundidad, nro_medicion")
                .setParameter("cod", sondajeCodigo)
                .getResultList();

        List<DesviacionSondajeDTO> out = new ArrayList<>(filas.size());
        for (Object[] r : filas) {
            DesviacionSondajeDTO d = new DesviacionSondajeDTO();
            d.setSondajeCodigo(str(r[0]));
            d.setSondajeNombre(str(r[1]));
            d.setProfundidad(r[2] == null ? null : new BigDecimal(r[2].toString()));
            d.setNroMedicion(r[3] == null ? null : ((Number) r[3]).shortValue());
            d.setFechaMedicion(date(r[4]));
            d.setMaquinaCodigo(str(r[5]));
            d.setPerforista(str(r[6]));
            d.setDesvAzimut(dbl(r[7]));
            d.setDesvAzimutSentido(str(r[8]));
            d.setDesvInclinacion(dbl(r[9]));
            d.setDesvInclinacionSentido(str(r[10]));
            d.setDesvMetros(dbl(r[11]));
            d.setDesvTotal(dbl(r[12]));
            d.setDecision(str(r[13]));
            d.setMedicionId(uid(r[14]));
            d.setSondajeId(uid(r[15]));
            d.setInterpretacion(str(r[16]));
            d.setInstruccion(str(r[17]));
            d.setResponsableSeguimiento(str(r[18]));
            d.setEstadoSeguimiento(str(r[19]));
            out.add(d);
        }
        return out;
    }

    // ---- helpers de mapeo defensivo desde JDBC ----

    /**
     * El driver de PostgreSQL devuelve los uuid como {@link java.util.UUID}, pero
     * una vista puede entregarlos como texto según cómo se resuelva el tipo de la
     * columna. Se contemplan los dos casos para no depender de eso.
     */
    private static java.util.UUID uid(Object o) {
        if (o == null) {
            return null;
        }
        return o instanceof java.util.UUID u ? u : java.util.UUID.fromString(o.toString());
    }

    private static String str(Object o) {
        return o == null ? null : o.toString();
    }

    private static Double dbl(Object o) {
        return o == null ? null : ((Number) o).doubleValue();
    }

    private static Long lng(Object o) {
        return o == null ? null : ((Number) o).longValue();
    }

    private static UUID uuid(Object o) {
        if (o == null) {
            return null;
        }
        return (o instanceof UUID u) ? u : UUID.fromString(o.toString());
    }

    private static LocalDate date(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof java.sql.Date d) {
            return d.toLocalDate();
        }
        if (o instanceof LocalDate ld) {
            return ld;
        }
        return LocalDate.parse(o.toString());
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * El plan no se guarda como serie: el sondaje se planifica con un azimut y
     * una inclinación de partida, y el trazo planificado es esa recta. Las dos
     * curvas medidas salen de {@code prf_survey}, que trae una estación cada
     * diez metros.
     * </p>
     *
     * <p>
     * El desvío que se informa es el de la estación más profunda, no el máximo
     * de la serie: el número que importa operativamente es hacia dónde va el
     * agujero ahora, no cuánto se apartó en algún punto del que ya volvió.
     * </p>
     */
    @Override
    @Transactional(readOnly = true)
    public TrayectoriaSondajeDTO trayectoria(UUID sondajeId) {
        log.debug("Consultando la trayectoria del sondaje ID: {}", sondajeId);

        Sondaje sondaje = sondajeRepository.findById(sondajeId)
                .orElseThrow(() -> new EntityNotFoundException("El Sondaje consultado no existe."));

        TrayectoriaSondajeDTO dto = new TrayectoriaSondajeDTO();
        dto.setSondajeId(sondaje.getId());
        dto.setSondajeCodigo(sondaje.getCodigo());
        dto.setSondajeNombre(sondaje.getNombre());
        dto.setAzimutPlan(sondaje.getAzimutSetup());
        dto.setInclinacionPlan(sondaje.getInclinacionSetup());
        dto.setProfundidadActual(sondaje.getProfundidadActual());
        dto.setProfundidadFinal(sondaje.getProfundidadFinal());

        List<Survey> estaciones = surveyRepository.findBySondajeIdOrderByProfundidadAsc(sondajeId);
        for (Survey s : estaciones) {
            EstacionSurveyDTO e = new EstacionSurveyDTO();
            e.setProfundidad(s.getProfundidad());
            e.setAzimut(s.getAzimut());
            e.setInclinacion(s.getInclinacion());
            e.setQc(s.getQc());
            dto.getEstaciones().add(e);
        }

        if (!estaciones.isEmpty()) {
            Survey ultima = estaciones.get(estaciones.size() - 1);
            dto.setProfundidadUltima(ultima.getProfundidad());
            if (sondaje.getAzimutSetup() != null && ultima.getAzimut() != null) {
                dto.setDesvAzimutUltimo(Math.abs(ultima.getAzimut() - sondaje.getAzimutSetup()));
            }
            if (sondaje.getInclinacionSetup() != null && ultima.getInclinacion() != null) {
                dto.setDesvInclinacionUltimo(
                        Math.abs(ultima.getInclinacion() - sondaje.getInclinacionSetup()));
            }
        }

        return dto;
    }
}
