package com.boulder.boulder.dto;

import java.util.UUID;

import lombok.Data;

/**
 * Objeto de transferencia de datos para un rol de usuario.
 *
 * <p>
 * Contiene la información básica de un rol dentro del sistema, utilizado
 * para controlar permisos y accesos.
 * </p>
 *
 * <p>
 * Se utiliza para la gestión de autorizaciones y perfiles de usuario.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Data
public class RolDTO {
    private UUID id;
    private Integer numeroInterno;
    private String codigo;
    private String nombre;
}
