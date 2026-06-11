package com.boulder.boulder.services;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boulder.boulder.dto.SurveyDTO;
import com.boulder.boulder.entities.Survey;
import com.boulder.boulder.entities.Sondaje;
import com.boulder.boulder.repositories.SurveyRepository;
import com.boulder.boulder.repositories.SondajeRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * Implementación de los servicios de negocio para la gestión de mediciones
 * de trayectoria.
 *
 * <p>
 * Gestiona las operaciones relacionadas con el registro y consulta de datos
 * de orientación y desviación de sondajes.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Service
public class SurveyServiceImpl implements SurveyService {

    private static final Logger log = LoggerFactory.getLogger(SurveyServiceImpl.class);

    @Autowired
    private SurveyRepository surveyRepository;

    @Autowired
    private SondajeRepository sondajeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SurveyDTO> listarTodos() {
        log.debug("Consultando el histórico completo de mediciones de survey en el proyecto");
        return surveyRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SurveyDTO obtenerPorId(UUID id) {
        log.debug("Buscando registro de survey por ID: {}", id);
        Survey survey = surveyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Estación de survey no localizada con el ID provisto."));
        return convertToDto(survey);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SurveyDTO> listarPorSondaje(UUID sondajeId) {
        log.debug("Recuperando trayectoria tridimensional del Sondaje ID: {}", sondajeId);
        return surveyRepository.findBySondajeIdOrderByProfundidadAsc(sondajeId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SurveyDTO guardar(SurveyDTO dto) {
        log.info("Registrando nueva estación de Survey a los {}m para el Sondaje ID: {}", dto.getProfundidad(), dto.getSondajeId());
        try {
            Survey survey = new Survey();
            
            survey.setProfundidad(dto.getProfundidad());
            survey.setAzimut(dto.getAzimut());
            survey.setInclinacion(dto.getInclinacion());
            survey.setMetodoMedicion(dto.getMetodoMedicion());

            if (dto.getSondajeId() != null) {
                Sondaje sondaje = sondajeRepository.findById(dto.getSondajeId())
                        .orElseThrow(() -> new EntityNotFoundException("No se puede registrar una medición si el Sondaje asignado no existe."));
                survey.setSondaje(sondaje);
            } else {
                throw new IllegalArgumentException("El ID del sondaje es mandatorio para registrar una medición de Survey.");
            }

            Survey guardado = surveyRepository.save(survey);
            log.info("Punto de survey registrado exitosamente. ID: {}, Profundidad: {}m", guardado.getId(), guardado.getProfundidad());
            return convertToDto(guardado);
        } catch (Exception e) {
            log.error("Error crítico al registrar la estación de trayectoria en profundidad: {}", dto.getProfundidad(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public SurveyDTO actualizar(UUID id, SurveyDTO dto) {
        log.info("Actualizando datos de calibración del Survey ID: {}", id);
        try {
            Survey survey = surveyRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Registro de survey inexistente en la base de datos."));

            survey.setProfundidad(dto.getProfundidad());
            survey.setAzimut(dto.getAzimut());
            survey.setInclinacion(dto.getInclinacion());
            survey.setMetodoMedicion(dto.getMetodoMedicion());

            if (dto.getSondajeId() != null && (survey.getSondaje() == null || !survey.getSondaje().getId().equals(dto.getSondajeId()))) {
                Sondaje nuevoSondaje = sondajeRepository.findById(dto.getSondajeId())
                        .orElseThrow(() -> new EntityNotFoundException("El nuevo Sondaje asociado no existe."));
                survey.setSondaje(nuevoSondaje);
            }

            Survey actualizado = surveyRepository.save(survey);
            log.info("Medición de survey ID: {} actualizada con éxito en el sistema", actualizado.getId());
            return convertToDto(actualizado);
        } catch (Exception e) {
            log.error("Error al modificar la estación de trayectoria ID: {}", id, e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void eliminar(UUID id) {
        log.warn("Solicitud de eliminación de estación de trayectoria ID: {}", id);
        Survey survey = surveyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se puede eliminar un registro de survey inexistente."));
        surveyRepository.delete(survey);
        log.info("Punto de survey ID: {} removido correctamente del histórico operativo", id);
    }

    private SurveyDTO convertToDto(Survey survey) {
        SurveyDTO dto = new SurveyDTO();
        dto.setId(survey.getId());
        dto.setNumeroInterno(survey.getNumeroInterno());
        dto.setProfundidad(survey.getProfundidad());
        dto.setAzimut(survey.getAzimut());
        dto.setInclinacion(survey.getInclinacion());
        dto.setMetodoMedicion(survey.getMetodoMedicion());

        if (survey.getSondaje() != null) {
            dto.setSondajeId(survey.getSondaje().getId());
        }

        return dto;
    }
}