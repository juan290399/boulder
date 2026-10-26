package com.boulder.boulder.services;

import java.util.List;
import java.util.UUID;

import com.boulder.boulder.dto.SurveyMedicionDTO;

/**
 * Operaciones de negocio para los eventos de medición de desviación de sondaje.
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
public interface SurveyMedicionService {
    List<SurveyMedicionDTO> listarTodos();
    SurveyMedicionDTO obtenerPorId(UUID id);
    List<SurveyMedicionDTO> listarPorSondaje(UUID sondajeId);
    SurveyMedicionDTO guardar(SurveyMedicionDTO dto);
    SurveyMedicionDTO actualizar(UUID id, SurveyMedicionDTO dto);
    void eliminar(UUID id);
}
