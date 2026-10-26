package com.boulder.boulder.dto.cuenta;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de {@code PUT /api/cuenta/contrasena}. No lleva el usuario: sale del
 * token, para que nadie pueda cambiar la contraseña de otra cuenta.
 *
 * @param actual contraseña actual
 * @param nueva  contraseña nueva; las reglas finas las aplica el servicio
 */
public record CambioContrasenaDTO(
        @NotBlank String actual,
        @NotBlank @Size(max = 128) String nueva) {

    /** Las contraseñas nunca aparecen en un log ni en un mensaje de error. */
    @Override
    public String toString() {
        return "CambioContrasenaDTO[actual=***, nueva=***]";
    }
}
