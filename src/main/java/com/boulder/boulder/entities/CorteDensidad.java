package com.boulder.boulder.entities;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Parte de corte de testigo para ensayo de densidad (parte de CU15).
 *
 * <p>
 * Va por separado del corte geoquímico porque la muestra de densidad es una
 * pieza entera y corta —de ahí que el metraje total sea de centímetros frente
 * a los cientos de metros del corte para análisis.
 * </p>
 *
 * <p>Se persiste en la tabla {@code operacional.crt_densidad}.</p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "crt_densidad", schema = "operacional")
@Getter
@Setter
public class CorteDensidad extends AvanceOperativo {

    @Column(length = 80)
    private String responsable;

    @Column(nullable = false, precision = 9, scale = 2)
    private BigDecimal desde;

    @Column(nullable = false, precision = 9, scale = 2)
    private BigDecimal hasta;

    @Column(name = "total_muestras", nullable = false)
    private Integer totalMuestras;

    @Column(name = "total_metraje", precision = 9, scale = 2)
    private BigDecimal totalMetraje;

    @Column(name = "tipo_linea", length = 10)
    private String tipoLinea;
}
