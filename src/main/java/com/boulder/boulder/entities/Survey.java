package com.boulder.boulder.entities;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Representa un registro de medición (survey) asociado a un sondaje.
 *
 * <p>
 * Almacena datos de control de trayectoria del sondaje como profundidad,
 * azimut e inclinación, permitiendo el seguimiento geológico de la perforación
 * a lo largo del tiempo.
 * </p>
 *
 * <p>
 * Se persiste en la tabla {@code operacional.prf_survey}.
 * </p>
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "prf_survey", schema = "operacional")
@Data
public class Survey implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "numero_interno", insertable = false, updatable = false)
    private Integer numeroInterno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sondaje_id", nullable = false)
    private Sondaje sondaje;

    @Column(nullable = false)
    private Double profundidad;

    @Column(nullable = false)
    private Double azimut;

    @Column(nullable = false)
    private Double inclinacion;

    @Column(name = "metodo_medicion", length = 50)
    private String metodoMedicion;
}
