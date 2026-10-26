package com.boulder.boulder.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

/**
 * Evolución de la desviación por sondaje y profundidad (vista
 * {@code operacional.vw_desviacion_por_sondaje}). Solo lectura.
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Data
public class DesviacionSondajeDTO {
    private String sondajeCodigo;
    private String sondajeNombre;
    private BigDecimal profundidad;
    private Short nroMedicion;
    private LocalDate fechaMedicion;
    private String maquinaCodigo;
    private String perforista;
    private Double desvAzimut;
    private String desvAzimutSentido;
    private Double desvInclinacion;
    private String desvInclinacionSentido;
    private Double desvMetros;
    private Double desvTotal;
    private String decision;

    /** Identificadores, para poder pedir la trayectoria y editar el seguimiento. */
    private java.util.UUID medicionId;
    private java.util.UUID sondajeId;

    /** Por qué se desvió; la lectura geológica, no el número. */
    private String interpretacion;

    /** Qué hacer; la indicación operativa para la máquina. */
    private String instruccion;

    private String responsableSeguimiento;

    /** sin_seguimiento | pendiente | en_curso | cerrado */
    private String estadoSeguimiento;
}
