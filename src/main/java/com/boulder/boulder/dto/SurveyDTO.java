package com.boulder.boulder.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class SurveyDTO {
    private UUID id;
    private UUID sondajeId;
    private Double profundidad;
    private Double azimut;
    private Double inclinacion;
    private String metodoMedicion;
}