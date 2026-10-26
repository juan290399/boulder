package com.boulder.boulder.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Objeto de transferencia para un parte de recepción de cajas de campo (CU12).
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RecepcionCajaDTO extends AvanceOperativoDTO {

    private String responsable;
    private Integer boxDesde;
    private Integer boxHasta;

    @PositiveOrZero(message = "El total de cajas no puede ser negativo")
    private Integer totalCajas;

    private BigDecimal marcadoDesde;
    private BigDecimal marcadoHasta;
    private BigDecimal totalMetraje;
}
