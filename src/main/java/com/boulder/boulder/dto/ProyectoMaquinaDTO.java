package com.boulder.boulder.dto;

import java.time.LocalDate;
import java.util.UUID;

import lombok.Data;

/**
 * Objeto de transferencia de datos para la asignación de una máquina a un proyecto.
 *
 * <p>
 * Contiene la relación entre un proyecto y una máquina, incluyendo fechas
 * de ingreso, salida y estado de asignación.
 * </p>
 *
 * <p>
 * Se utiliza para gestionar la disponibilidad y asignación de equipos en
 * los proyectos.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Data
public class ProyectoMaquinaDTO {
    private UUID id;
    private Integer numeroInterno;
    private UUID proyectoId;
    private UUID maquinaId;
    private LocalDate fechaIngreso;
    private LocalDate fechaSalida;
    private String estado;
}
