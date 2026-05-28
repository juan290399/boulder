package com.boulder.boulder.services;

import java.util.List;
import java.util.UUID;

import com.boulder.boulder.dto.SurveyDTO;

public interface SurveyService {
    List<SurveyDTO> obtenerPorSondajeOrdenado(UUID sondajeId);
    SurveyDTO guardar(SurveyDTO dto);
}