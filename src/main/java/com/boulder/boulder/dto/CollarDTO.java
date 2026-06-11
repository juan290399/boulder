package com.boulder.boulder.dto;

import java.time.LocalDate;
import java.util.UUID;

import lombok.Data;

/**
 * Objeto de transferencia de datos para un collar de perforación.
 *
 * <p>
 * Contiene la información del punto inicial de un sondaje, incluyendo
 * coordenadas geográficas, elevación y datos de medición.
 * </p>
 *
 * <p>
 * Se utiliza para transportar información del collar entre las capas de
 * la aplicación sin exponer la lógica de persistencia.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Data
public class CollarDTO {
    private UUID id;
    private Integer numeroInterno;
    private String codigo;
    private String nombre;
    private Double este;
    private Double norte;
    private Double elevacion;
    private LocalDate fechaMedicion;
    private String campania;
    private String notas;
}
