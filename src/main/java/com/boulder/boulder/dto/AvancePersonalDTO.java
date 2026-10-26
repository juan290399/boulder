package com.boulder.boulder.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Objeto de transferencia de datos para un integrante de la dotación de un
 * parte diario de perforación.
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Data
public class AvancePersonalDTO {
    private UUID id;
    private UUID perforistaId;

    @NotBlank(message = "El nombre del integrante es obligatorio")
    private String nombre;

    private String cargo;
    private String codigoMaquina;
}
