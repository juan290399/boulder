package com.boulder.boulder.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Objeto de transferencia para un parte diario de avance de logueo (CU18).
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AvanceLogueoDTO extends AvanceOperativoDTO {

    @NotBlank(message = "El tipo de logueo es obligatorio")
    private String tipoLogueo;

    @NotNull(message = "La profundidad 'desde' es obligatoria")
    private BigDecimal desde;

    @NotNull(message = "La profundidad 'hasta' es obligatoria")
    private BigDecimal hasta;

    @NotNull(message = "El avance del turno es obligatorio")
    @PositiveOrZero(message = "El avance no puede ser negativo")
    private BigDecimal avance;

    private String geologo;

    @DecimalMin(value = "0", message = "El porcentaje completado no puede ser negativo")
    @DecimalMax(value = "100", message = "El porcentaje completado no puede pasar de 100")
    private BigDecimal pctCompletado;
}
