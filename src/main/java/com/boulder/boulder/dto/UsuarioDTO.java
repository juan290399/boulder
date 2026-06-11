package com.boulder.boulder.dto;

import java.util.List;
import java.util.UUID;

import lombok.Data;

/**
 * Objeto de transferencia de datos para un usuario del sistema.
 *
 * <p>
 * Contiene la información personal, credenciales de acceso, estado de la
 * cuenta y roles asignados al usuario.
 * </p>
 *
 * <p>
 * Se utiliza para la gestión de usuarios, autenticación y autorización
 * dentro de la aplicación.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Data
public class UsuarioDTO {
    private UUID id;
    private Integer numeroInterno;
    private String nombres;
    private String apellidos;
    private String correo;
    private String usuario;
    private String contrasenia;
    private Boolean estado;
    private List<String> roles;
}
