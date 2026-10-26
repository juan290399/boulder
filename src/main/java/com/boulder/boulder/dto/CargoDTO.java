package com.boulder.boulder.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Objeto de transferencia de datos para un puesto laboral.
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Data
public class CargoDTO {
    private UUID id;
    private Integer numeroInterno;

    @NotBlank(message = "El código es obligatorio")
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    /** perforacion / geologia / laboratorio / administracion */
    @NotBlank(message = "El área es obligatoria")
    private String area;

    private Boolean estado;
}
