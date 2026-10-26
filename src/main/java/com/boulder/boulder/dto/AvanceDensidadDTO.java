package com.boulder.boulder.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Objeto de transferencia para un parte de estufa / densidad (UC20).
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AvanceDensidadDTO extends AvanceOperativoDTO {

    private String responsable;

    @NotBlank(message = "La etapa del ensayo es obligatoria")
    private String etapa;

    @NotNull(message = "El número de muestras es obligatorio")
    @PositiveOrZero(message = "El número de muestras no puede ser negativo")
    private Integer nroMuestras;

    private BigDecimal temperaturaC;
    private String tipoLinea;
    private BigDecimal desde;
    private BigDecimal hasta;
}
