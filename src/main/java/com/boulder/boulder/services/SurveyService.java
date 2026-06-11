package com.boulder.boulder.services;

import java.util.List;
import java.util.UUID;

import com.boulder.boulder.dto.SurveyDTO;

/**
 * Define las operaciones de negocio relacionadas con la gestión de mediciones
 * de trayectoria.
 *
 * <p>
 * Proporciona funcionalidades para administrar los registros de survey
 * asociados a los sondajes.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
public interface SurveyService {
    List<SurveyDTO> listarTodos();
    SurveyDTO obtenerPorId(UUID id);
    List<SurveyDTO> listarPorSondaje(UUID sondajeId);
    SurveyDTO guardar(SurveyDTO dto);
    SurveyDTO actualizar(UUID id, SurveyDTO dto);
    void eliminar(UUID id);
}