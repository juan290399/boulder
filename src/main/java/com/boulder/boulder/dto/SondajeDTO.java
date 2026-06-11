package com.boulder.boulder.dto;

import java.time.LocalDate;
import java.util.UUID;

import lombok.Data;

/**
 * Objeto de transferencia de datos para un sondaje.
 *
 * <p>
 * Contiene la información operativa y técnica de una perforación,
 * incluyendo parámetros de diseño, profundidades, fechas y recursos
 * asociados.
 * </p>
 *
 * <p>
 * Se utiliza para transportar información de sondajes entre los servicios,
 * controladores y clientes de la aplicación.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Data
public class SondajeDTO {
    private UUID id;
    private Integer numeroInterno;
    private UUID proyectoId;
    private UUID programaSondajeId;
    private UUID collarId;
    private UUID plataformaId;
    private UUID maquinaId;
    private String codigo;
    private String nombre;
    private Double azimutSetup;
    private Double inclinacionSetup;
    private Double profundidadActual;
    private Double profundidadFinal;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String notas;
}