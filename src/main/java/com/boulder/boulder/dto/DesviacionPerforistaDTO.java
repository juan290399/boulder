package com.boulder.boulder.dto;

import java.util.UUID;

import lombok.Data;

/**
 * Resumen de desviación agregada por perforista (vista
 * {@code operacional.vw_desviacion_por_perforista}). Solo lectura.
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Data
public class DesviacionPerforistaDTO {
    private UUID perforistaId;
    private String perforistaCodigo;
    private String perforista;
    private Long nMediciones;
    private Double desvAzPromAbs;
    private Double desvAzMaxAbs;
    private Double desvDipPromAbs;
    private Double desvDipMaxAbs;
    private Double desvMetrosProm;
    private Double desvMetrosMax;
    private Long nCorrecciones;
}
