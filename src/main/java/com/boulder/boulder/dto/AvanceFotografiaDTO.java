package com.boulder.boulder.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Objeto de transferencia para un parte de registro fotográfico (CU13).
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AvanceFotografiaDTO extends AvanceOperativoDTO {

    private String responsable;
    private Integer boxDesde;
    private Integer boxHasta;

    @PositiveOrZero(message = "El total de cajas no puede ser negativo")
    private Integer totalCajas;

    private BigDecimal marcadoDesde;
    private BigDecimal marcadoHasta;
    private BigDecimal totalMetraje;

    /** seco | humedo */
    private String secoHumedo;

    @PositiveOrZero(message = "El número de fotos no puede ser negativo")
    private Integer nroFotos;

    private String rutaArchivos;
}
