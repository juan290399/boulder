package com.boulder.boulder.dto;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Objeto de transferencia de datos para un empleado.
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Data
public class EmpleadoDTO {
    private UUID id;
    private Integer numeroInterno;

    @NotBlank(message = "El código es obligatorio")
    private String codigo;

    @NotBlank(message = "Los nombres son obligatorios")
    private String nombres;

    private String apellidos;
    private String documento;
    private UUID empresaId;
    private UUID cargoId;

    /** Solo lectura: se rellena al convertir, para no obligar al cliente a
     *  cruzar el catálogo de cargos por su cuenta. */
    private String cargoNombre;
    private String cargoArea;

    @Email(message = "El correo no tiene un formato válido")
    private String email;

    private String telefono;
    private LocalDate fechaIngreso;
    private LocalDate fechaSalida;
    private Boolean estado;
    private String notas;
}
