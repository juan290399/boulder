package com.boulder.boulder.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Objeto de transferencia para un parte de corte geoquímico (UC18).
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CorteMuestraDTO extends AvanceOperativoDTO {

    private String responsable;

    @NotNull(message = "La profundidad 'desde' es obligatoria")
    private BigDecimal desde;

    @NotNull(message = "La profundidad 'hasta' es obligatoria")
    private BigDecimal hasta;

    private String tipoLinea;
    private BigDecimal metrajeEstimado;

    @PositiveOrZero(message = "El número de muestras no puede ser negativo")
    private Integer nroMuestras;
}
