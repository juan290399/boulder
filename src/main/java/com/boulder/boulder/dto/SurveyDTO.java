package com.boulder.boulder.dto;

import java.util.UUID;

import lombok.Data;

/**
 * Objeto de transferencia de datos para una medición de sondaje.
 *
 * <p>
 * Contiene los datos de profundidad, azimut e inclinación registrados
 * durante la perforación.
 * </p>
 *
 * <p>
 * Se utiliza para transportar mediciones de trayectoria entre las capas
 * del sistema.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Data
public class SurveyDTO {
    private UUID id;
    private Integer numeroInterno;
    private UUID sondajeId;
    private Double profundidad;
    private Double azimut;
    private Double inclinacion;
    private String metodoMedicion;
}