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
 * Representa un programa de sondajes dentro de un proyecto de exploración.
 *
 * <p>
 * Define la planificación de perforaciones asociadas a una plataforma,
 * incluyendo su ubicación, parámetros geométricos (azimut, inclinación,
 * profundidad) y clasificación operativa (prioridad, zona, flanco).
 * </p>
 *
 * <p>
 * Se almacena en la tabla {@code operacional.prf_programa_sondaje}.
 * </p>
 * 
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "prf_programa_sondaje", schema = "operacional")
@Data
public class ProgramaSondaje implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "numero_interno", insertable = false, updatable = false)
    private Integer numeroInterno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plataforma_id", nullable = false)
    private Plataforma plataforma;

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

    @Column(nullable = false)
    private Double profundidad;

    @Column(nullable = false)
    private Double azimut;

    @Column(nullable = false)
    private Double inclinacion;

    @Column(length = 50)
    private String plano;

    @Column(length = 50)
    private String punto;

    @Column(length = 30)
    private String prioridad;

    @Column(length = 50)
    private String zona;

    @Column(length = 50)
    private String flanco;

    @Column(columnDefinition = "TEXT")
    private String notas;
}