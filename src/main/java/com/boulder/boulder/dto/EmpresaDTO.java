package com.boulder.boulder.dto;

import java.util.UUID;

import lombok.Data;

/**
 * Objeto de transferencia de datos para una empresa.
 *
 * <p>
 * Contiene la información básica de identificación y contacto de una
 * empresa, incluyendo RUC, dirección y medios de comunicación.
 * </p>
 *
 * <p>
 * Se utiliza para intercambiar información de empresas entre las capas
 * del sistema.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Data
public class EmpresaDTO {
    private UUID id;
    private Integer numeroInterno;
    private String codigo;
    private String nombre;
    private String ruc;
    private String direccion;
    private String telefono;
    private String correo;
}
