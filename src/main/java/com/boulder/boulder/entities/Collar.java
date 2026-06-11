package com.boulder.boulder.entities;

import java.io.Serializable;
import java.time.LocalDate;
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
 * Representa un collar de perforación dentro de una campaña de exploración.
 *
 * <p>Un collar corresponde al punto georreferenciado donde inicia un sondaje,
 * almacenando sus coordenadas, elevación y datos asociados a la medición
 * topográfica.</p>
 *
 * <p>Se persiste en la tabla {@code operacional.prf_collar}.</p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "prf_collar", schema = "operacional")
@Data
public class Collar implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "numero_interno", insertable = false, updatable = false)
    private Integer numeroInterno;

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false)
    private Double este;

    @Column(nullable = false)
    private Double norte;

    @Column(nullable = false)
    private Double elevacion;

    @Column(name = "fecha_medicion")
    private LocalDate fechaMedicion;

    @Column(length = 50)
    private String campania;

    @Column(columnDefinition = "TEXT")
    private String notas;
}