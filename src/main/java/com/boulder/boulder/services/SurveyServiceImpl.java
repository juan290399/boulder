package com.boulder.boulder.services;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boulder.boulder.dto.SurveyDTO;
import com.boulder.boulder.entities.Sondaje;
import com.boulder.boulder.entities.Survey;
import com.boulder.boulder.repositories.SondajeRepository;
import com.boulder.boulder.repositories.SurveyRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class SurveyServiceImpl implements SurveyService {

    @Autowired private SurveyRepository surveyRepository;
    @Autowired private SondajeRepository sondajeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SurveyDTO> obtenerPorSondajeOrdenado(UUID sondajeId) {
        return surveyRepository.findBySondajeIdOrderByProfundidadAsc(sondajeId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SurveyDTO guardar(SurveyDTO dto) {
        Sondaje sondaje = sondajeRepository.findById(dto.getSondajeId())
                .orElseThrow(() -> new EntityNotFoundException("Sondaje no existe para asociar la trayectoria"));

        Survey survey = new Survey();
        survey.setSondaje(sondaje);
        survey.setProfundidad(dto.getProfundidad());
        survey.setAzimut(dto.getAzimut());
        survey.setInclinacion(dto.getInclinacion());
        survey.setMetodoMedicion(dto.getMetodoMedicion());

        return convertToDto(surveyRepository.save(survey));
    }

    private SurveyDTO convertToDto(Survey survey) {
        SurveyDTO dto = new SurveyDTO();
        dto.setId(survey.getId());
        dto.setSondajeId(survey.getSondaje().getId());
        dto.setProfundidad(survey.getProfundidad());
        survey.setAzimut(survey.getAzimut());
        survey.setInclinacion(survey.getInclinacion());
        dto.setMetodoMedicion(survey.getMetodoMedicion());
        return dto;
    }
}