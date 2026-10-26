package com.boulder.boulder.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boulder.boulder.dominio.Desviacion;
import com.boulder.boulder.dominio.Revision;
import com.boulder.boulder.dto.SurveyMedicionDTO;
import com.boulder.boulder.entities.SurveyMedicion;
import com.boulder.boulder.repositories.MaquinaRepository;
import com.boulder.boulder.repositories.EmpleadoRepository;
import com.boulder.boulder.repositories.SondajeRepository;
import com.boulder.boulder.repositories.SurveyMedicionRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * Implementación de los servicios de negocio para los eventos de medición de
 * desviación de sondaje.
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Service
public class SurveyMedicionServiceImpl implements SurveyMedicionService {

    private static final Logger log = LoggerFactory.getLogger(SurveyMedicionServiceImpl.class);

    private final SurveyMedicionRepository medicionRepository;
    private final SondajeRepository sondajeRepository;
    private final MaquinaRepository maquinaRepository;
    private final EmpleadoRepository empleadoRepository;

    public SurveyMedicionServiceImpl(SurveyMedicionRepository medicionRepository, SondajeRepository sondajeRepository,
            MaquinaRepository maquinaRepository, EmpleadoRepository empleadoRepository) {
        this.medicionRepository = medicionRepository;
        this.sondajeRepository = sondajeRepository;
        this.maquinaRepository = maquinaRepository;
        this.empleadoRepository = empleadoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SurveyMedicionDTO> listarTodos() {
        log.debug("Consultando el histórico de mediciones de desviación");
        return medicionRepository.findAll().stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SurveyMedicionDTO obtenerPorId(UUID id) {
        log.debug("Buscando medición de desviación ID: {}", id);
        return convertToDto(buscar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SurveyMedicionDTO> listarPorSondaje(UUID sondajeId) {
        log.debug("Consultando mediciones de desviación del sondaje ID: {}", sondajeId);
        return medicionRepository.findBySondajeIdOrderByProfundidadAscNroMedicionAsc(sondajeId).stream()
                .map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SurveyMedicionDTO guardar(SurveyMedicionDTO dto) {
        log.info("Registrando medición de desviación. Sondaje ID: {}, Profundidad: {}",
                dto.getSondajeId(), dto.getProfundidad());
        validar(dto, null);
        SurveyMedicion medicion = new SurveyMedicion();
        aplicar(dto, medicion);
        SurveyMedicion guardada = medicionRepository.save(medicion);
        // El INSERT se manda ya para devolver el número interno de la secuencia.
        medicionRepository.flush();
        return convertToDto(guardada);
    }

    @Override
    @Transactional
    public SurveyMedicionDTO actualizar(UUID id, SurveyMedicionDTO dto) {
        log.info("Actualizando medición de desviación ID: {}", id);
        SurveyMedicion medicion = buscar(id);
        validar(dto, id);
        aplicar(dto, medicion);
        return convertToDto(medicionRepository.save(medicion));
    }

    @Override
    @Transactional
    public void eliminar(UUID id) {
        log.warn("Eliminando medición de desviación ID: {}", id);
        medicionRepository.delete(buscar(id));
    }

    /**
     * Rechaza la medición con todos sus motivos juntos, numera la estación si no
     * viene numerada y evita cargar dos veces la misma.
     *
     * @param idActual la medición que se corrige, para no compararla consigo misma
     */
    private void validar(SurveyMedicionDTO dto, UUID idActual) {
        List<String> errores = new ArrayList<>(new Revision()
                .opcion("desv_azimut_sentido", dto.getDesvAzimutSentido(), "izquierda", "derecha")
                .opcion("desv_inclinacion_sentido", dto.getDesvInclinacionSentido(), "arriba", "abajo")
                .opcion("decision", dto.getDecision(), "continuar", "corregir", "evaluar")
                .opcion("estado_seguimiento", dto.getEstadoSeguimiento(),
                        "sin_seguimiento", "pendiente", "en_curso", "cerrado")
                .errores());
        fueraDeRango(errores, "azimut_medido", dto.getAzimutMedido(), 0, 360);
        fueraDeRango(errores, "azimut_plan", dto.getAzimutPlan(), 0, 360);
        fueraDeRango(errores, "inclinacion_medida", dto.getInclinacionMedida(), -90, 90);
        fueraDeRango(errores, "inclinacion_plan", dto.getInclinacionPlan(), -90, 90);
        if (dto.getProfundidad() != null && dto.getProfundidad().signum() < 0) {
            errores.add("profundidad: no puede ser negativa");
        }
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join("; ", errores));
        }

        List<SurveyMedicion> enLaEstacion = medicionRepository
                .findBySondajeIdOrderByProfundidadAscNroMedicionAsc(dto.getSondajeId()).stream()
                .filter(m -> !Objects.equals(m.getId(), idActual))
                .filter(m -> m.getProfundidad().compareTo(dto.getProfundidad()) == 0)
                .toList();
        if (dto.getNroMedicion() == null) {
            // Se repite la medición en la misma estación cuando la primera sale
            // dudosa; la nueva es la siguiente, no otra «1».
            dto.setNroMedicion((short) (enLaEstacion.stream()
                    .map(SurveyMedicion::getNroMedicion).filter(Objects::nonNull)
                    .mapToInt(Short::intValue).max().orElse(0) + 1));
        } else if (enLaEstacion.stream().anyMatch(m -> dto.getNroMedicion().equals(m.getNroMedicion()))) {
            throw new IllegalStateException("Ya hay una medición N.º " + dto.getNroMedicion() + " a "
                    + dto.getProfundidad().toPlainString() + " m en este sondaje.");
        }
    }

    private static void fueraDeRango(List<String> errores, String campo, Double valor, double min, double max) {
        if (valor != null && (valor < min || valor > max)) {
            errores.add(campo + ": tiene que estar entre " + (int) min + " y " + (int) max + " grados");
        }
    }

    /**
     * Con azimut e inclinación medidos, el desvío se calcula; no se confía en uno
     * tecleado. El plan es el que viene o, si no, el setup del sondaje. Sin
     * medidos (los partes viejos solo traen el desvío) se deja lo escrito.
     */
    private void calcularDesvio(SurveyMedicion m) {
        if (m.getAzimutMedido() == null || m.getInclinacionMedida() == null) {
            return;
        }
        if (m.getAzimutPlan() == null) {
            m.setAzimutPlan(m.getSondaje().getAzimutSetup());
        }
        if (m.getInclinacionPlan() == null) {
            m.setInclinacionPlan(m.getSondaje().getInclinacionSetup());
        }
        if (m.getAzimutPlan() == null || m.getInclinacionPlan() == null) {
            return;
        }
        Desviacion d = Desviacion.calcular(m.getProfundidad().doubleValue(), m.getAzimutMedido(),
                m.getInclinacionMedida(), m.getAzimutPlan(), m.getInclinacionPlan());
        m.setDesvAzimut(redondear(d.azimut(), 4));
        m.setDesvAzimutSentido(d.sentidoAzimut());
        m.setDesvInclinacion(redondear(d.inclinacion(), 4));
        m.setDesvInclinacionSentido(d.sentidoInclinacion());
        m.setDesvTotal(redondear(d.total(), 4));
        m.setDesvMetros(redondear(d.metros(), 2));
    }

    private static double redondear(double valor, int decimales) {
        return BigDecimal.valueOf(valor).setScale(decimales, RoundingMode.HALF_UP).doubleValue();
    }

    private SurveyMedicion buscar(UUID id) {
        return medicionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Medición de desviación no encontrada con ID: " + id));
    }

    private void aplicar(SurveyMedicionDTO dto, SurveyMedicion medicion) {
        medicion.setSondaje(sondajeRepository.findById(dto.getSondajeId())
                .orElseThrow(() -> new EntityNotFoundException("El Sondaje asignado no existe.")));
        medicion.setMaquina(dto.getMaquinaId() == null ? null
                : maquinaRepository.findById(dto.getMaquinaId())
                        .orElseThrow(() -> new EntityNotFoundException("La Máquina asignada no existe.")));
        medicion.setPerforista(dto.getPerforistaId() == null ? null
                : empleadoRepository.findById(dto.getPerforistaId())
                        .orElseThrow(() -> new EntityNotFoundException("El empleado asignado como perforista no existe.")));

        medicion.setProfundidad(dto.getProfundidad());
        medicion.setNroMedicion(dto.getNroMedicion());
        medicion.setFechaMedicion(dto.getFechaMedicion());
        medicion.setFechaHora(dto.getFechaHora());
        medicion.setResponsable(dto.getResponsable());
        medicion.setMetodo(dto.getMetodo());
        medicion.setInstrumento(dto.getInstrumento());
        medicion.setAzimutMedido(dto.getAzimutMedido());
        medicion.setInclinacionMedida(dto.getInclinacionMedida());
        medicion.setAzimutPlan(dto.getAzimutPlan());
        medicion.setInclinacionPlan(dto.getInclinacionPlan());
        medicion.setDesvAzimut(dto.getDesvAzimut());
        medicion.setDesvAzimutSentido(dto.getDesvAzimutSentido());
        medicion.setDesvInclinacion(dto.getDesvInclinacion());
        medicion.setDesvInclinacionSentido(dto.getDesvInclinacionSentido());
        medicion.setDesvMetros(dto.getDesvMetros());
        medicion.setDesvTotal(dto.getDesvTotal());
        medicion.setEsPlanificada(dto.getEsPlanificada() != null ? dto.getEsPlanificada() : Boolean.FALSE);
        medicion.setDecision(dto.getDecision());
        medicion.setObservaciones(dto.getObservaciones());
        medicion.setInterpretacion(dto.getInterpretacion());
        medicion.setInstruccion(dto.getInstruccion());
        medicion.setResponsableSeguimiento(dto.getResponsableSeguimiento());
        // Una medicion sin instruccion no necesita seguimiento; solo pasa a
        // pendiente cuando alguien escribe que hay algo que hacer.
        medicion.setEstadoSeguimiento(dto.getEstadoSeguimiento() != null
                ? dto.getEstadoSeguimiento()
                : (dto.getInstruccion() != null && !dto.getInstruccion().isBlank()
                        ? "pendiente" : "sin_seguimiento"));
        calcularDesvio(medicion);
    }

    private SurveyMedicionDTO convertToDto(SurveyMedicion m) {
        SurveyMedicionDTO dto = new SurveyMedicionDTO();
        dto.setId(m.getId());
        dto.setNumeroInterno(m.getNumeroInterno());
        if (m.getSondaje() != null) {
            dto.setSondajeId(m.getSondaje().getId());
        }
        if (m.getMaquina() != null) {
            dto.setMaquinaId(m.getMaquina().getId());
        }
        if (m.getPerforista() != null) {
            dto.setPerforistaId(m.getPerforista().getId());
        }
        dto.setProfundidad(m.getProfundidad());
        dto.setNroMedicion(m.getNroMedicion());
        dto.setFechaMedicion(m.getFechaMedicion());
        dto.setFechaHora(m.getFechaHora());
        dto.setResponsable(m.getResponsable());
        dto.setMetodo(m.getMetodo());
        dto.setInstrumento(m.getInstrumento());
        dto.setAzimutMedido(m.getAzimutMedido());
        dto.setInclinacionMedida(m.getInclinacionMedida());
        dto.setAzimutPlan(m.getAzimutPlan());
        dto.setInclinacionPlan(m.getInclinacionPlan());
        dto.setDesvAzimut(m.getDesvAzimut());
        dto.setDesvAzimutSentido(m.getDesvAzimutSentido());
        dto.setDesvInclinacion(m.getDesvInclinacion());
        dto.setDesvInclinacionSentido(m.getDesvInclinacionSentido());
        dto.setDesvMetros(m.getDesvMetros());
        dto.setDesvTotal(m.getDesvTotal());
        dto.setEsPlanificada(m.getEsPlanificada());
        dto.setDecision(m.getDecision());
        dto.setObservaciones(m.getObservaciones());
        dto.setInterpretacion(m.getInterpretacion());
        dto.setInstruccion(m.getInstruccion());
        dto.setResponsableSeguimiento(m.getResponsableSeguimiento());
        dto.setEstadoSeguimiento(m.getEstadoSeguimiento());
        return dto;
    }
}
