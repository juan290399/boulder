package com.boulder.boulder.dto;

import java.util.UUID;

import lombok.Data;

/**
 * Objeto de transferencia de datos para un programa de sondaje.
 *
 * <p>
 * Contiene la planificación de perforaciones, incluyendo ubicación,
 * profundidad, orientación y parámetros técnicos definidos para la
 * ejecución de sondajes.
 * </p>
 *
 * <p>
 * Se utiliza para intercambiar información de planificación entre las
 * capas del sistema.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Data
public class ProgramaSondajeDTO {
    private UUID id;
    private Integer numeroInterno;
    private UUID proyectoId;
    private UUID plataformaId;
    private String codigo;
    private String nombre;
    private Double este;
    private Double norte;
    private Double elevacion;
    private Double profundidad;
    private Double azimut;
    private Double inclinacion;
    private String plano;
    private String punto;
    private String prioridad;
    private String zona;
    private String flanco;
    private String notas;
}
