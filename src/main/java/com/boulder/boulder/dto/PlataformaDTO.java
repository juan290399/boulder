package com.boulder.boulder.dto;

import java.util.UUID;

import lombok.Data;

/**
 * Objeto de transferencia de datos para una plataforma de perforación.
 *
 * <p>
 * Contiene la información geográfica, dimensional y operativa de una
 * plataforma utilizada para la ejecución de sondajes.
 * </p>
 *
 * <p>
 * Se utiliza para transferir datos de infraestructura entre capas de la
 * aplicación.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Data
public class PlataformaDTO {
    private UUID id;
    private Integer numeroInterno;
    private UUID proyectoId;
    private String codigo;
    private String nombre;
    private Double este;
    private Double norte;
    private Double elevacion;
    private Double ejeMayor;
    private Double ejeMinor;
    private Double ejeZ;
    private Double area;
    private Short numeroPozas;
    private String estado;
    private String tipo;
    private String ubicacion;
    private String notas;
}
