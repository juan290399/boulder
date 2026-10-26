package com.boulder.boulder.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Objeto de transferencia de datos para un evento de medición de desviación de
 * sondaje.
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Data
public class SurveyMedicionDTO {
    private UUID id;
    private Integer numeroInterno;

    @NotNull(message = "El sondaje es obligatorio")
    private UUID sondajeId;

    private UUID maquinaId;
    private UUID perforistaId;

    @NotNull(message = "La profundidad de la estación es obligatoria")
    private BigDecimal profundidad;

    private Short nroMedicion;

    @NotNull(message = "La fecha de medición es obligatoria")
    private LocalDate fechaMedicion;

    private OffsetDateTime fechaHora;
    private String responsable;
    private String metodo;
    private String instrumento;
    private Double azimutMedido;
    private Double inclinacionMedida;
    private Double azimutPlan;
    private Double inclinacionPlan;
    private Double desvAzimut;
    private String desvAzimutSentido;
    private Double desvInclinacion;
    private String desvInclinacionSentido;
    private Double desvMetros;
    private Double desvTotal;
    private Boolean esPlanificada;
    private String decision;
    private String observaciones;

    /** Por qué se desvió; la lectura geológica, no el número. */
    private String interpretacion;

    /** Qué hacer; la indicación operativa para la máquina. */
    private String instruccion;

    private String responsableSeguimiento;

    /** sin_seguimiento | pendiente | en_curso | cerrado */
    private String estadoSeguimiento;
}
