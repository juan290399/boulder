package com.boulder.boulder.services;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    private SurveyMedicionRepository medicionRepository;
    @Autowired
    private SondajeRepository sondajeRepository;
    @Autowired
    private MaquinaRepository maquinaRepository;
    @Autowired
    private EmpleadoRepository empleadoRepository;

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
        SurveyMedicion medicion = new SurveyMedicion();
        aplicar(dto, medicion);
        return convertToDto(medicionRepository.save(medicion));
    }

    @Override
    @Transactional
    public SurveyMedicionDTO actualizar(UUID id, SurveyMedicionDTO dto) {
        log.info("Actualizando medición de desviación ID: {}", id);
        SurveyMedicion medicion = buscar(id);
        aplicar(dto, medicion);
        return convertToDto(medicionRepository.save(medicion));
    }

    @Override
    @Transactional
    public void eliminar(UUID id) {
        log.warn("Eliminando medición de desviación ID: {}", id);
        medicionRepository.delete(buscar(id));
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
