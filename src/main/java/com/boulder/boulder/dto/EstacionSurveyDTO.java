package com.boulder.boulder.dto;

import lombok.Data;

/**
 * Una estación de la trayectoria medida de un sondaje.
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Data
public class EstacionSurveyDTO {

    private Double profundidad;
    private Double azimut;
    private Double inclinacion;

    /** Resultado del control de calidad de la medición: Pass, Fail... */
    private String qc;
}
