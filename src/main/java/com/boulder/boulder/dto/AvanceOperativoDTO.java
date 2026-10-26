package com.boulder.boulder.dto;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Cabecera común de los partes de avance operativo (UC16 a UC20).
 *
 * <p>
 * Reúne los campos que comparten los siete partes del flujo posterior a la
 * perforación. La validación de que venga el sondaje por ID o por código no se
 * declara aquí con anotaciones porque es una condición cruzada entre dos
 * campos: la comprueba el servicio, que además devuelve un mensaje que dice
 * cuál de los dos falta.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Data
public abstract class AvanceOperativoDTO {

    private UUID id;
    private Integer numeroInterno;

    private UUID sondajeId;
    private String sondajeCodigo;

    /** Código canónico del sondaje relacionado; solo lectura, lo llena el servicio. */
    private String sondajeCodigoReal;

    @NotNull(message = "La fecha del parte es obligatoria")
    private LocalDate fecha;

    private String turno;
    private String estado;
    private String observaciones;
}
