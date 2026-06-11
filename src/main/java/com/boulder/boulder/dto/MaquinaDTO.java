package com.boulder.boulder.dto;

import java.time.LocalDate;
import java.util.UUID;

import lombok.Data;

/**
 * Objeto de transferencia de datos para una máquina de perforación.
 *
 * <p>
 * Contiene la información técnica, operativa y administrativa de una
 * máquina utilizada en campañas de perforación.
 * </p>
 *
 * <p>
 * Se utiliza para transportar datos de equipos entre servicios,
 * controladores y clientes.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Data
public class MaquinaDTO {
    private UUID id;
    private Integer numeroInterno;
    private String codigo;
    private String nombre;
    private String tipo;
    private Short alcancePq;
    private Short alcanceHq;
    private Short alcanceNq;
    private String marca;
    private String motor;
    private LocalDate fechaCompra;
    private LocalDate fechaLlegada;
    private LocalDate fechaInicio;
    private String estado;
    private String notes;
}