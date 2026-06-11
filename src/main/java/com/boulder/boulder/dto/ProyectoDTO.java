package com.boulder.boulder.dto;

import java.util.UUID;

import lombok.Data;

/**
 * Objeto de transferencia de datos para un proyecto de exploración geológica.
 *
 * <p>
 * Contiene la información principal de un proyecto, incluyendo su
 * identificación, ubicación, sistema de referencia espacial y empresa
 * asociada.
 * </p>
 *
 * <p>
 * Se utiliza para intercambiar información de proyectos entre las capas
 * de la aplicación y las interfaces cliente.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Data
public class ProyectoDTO {
    private UUID id;
    private Integer numeroInterno;
    private UUID empresaId;
    private String codigo;
    private String nombre;
    private String ubicacion;
    private Integer srid;
}
