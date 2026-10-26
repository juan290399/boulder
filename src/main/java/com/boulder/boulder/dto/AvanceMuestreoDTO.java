package com.boulder.boulder.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Objeto de transferencia para un parte de muestreo geoquímico (CU17).
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AvanceMuestreoDTO extends AvanceOperativoDTO {

    private String responsable;

    @NotBlank(message = "El estado del muestreo es obligatorio")
    private String estadoMuestreo;

    private BigDecimal desde;
    private BigDecimal hasta;
    private BigDecimal avance;

    @PositiveOrZero(message = "El número de muestras no puede ser negativo")
    private Integer nroMuestras;

    @PositiveOrZero(message = "El número de muestras embolsadas no puede ser negativo")
    private Integer nroMuestrasEmbolsadas;

    @PositiveOrZero(message = "El número de costales no puede ser negativo")
    private Integer nroCostales;

    @PositiveOrZero(message = "El número de lotes no puede ser negativo")
    private Integer nroLotes;
}
