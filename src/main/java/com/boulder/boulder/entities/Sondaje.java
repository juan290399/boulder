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
 * Entidad que representa un sondaje de exploración geológica.
 *
 * <p>
 * Un sondaje corresponde a una perforación ejecutada dentro de un proyecto,
 * asociada a un programa de sondaje, collar, plataforma y máquina de perforación.
 * Permite registrar información de diseño, avance y control operacional.
 * </p>
 *
 * <p>
 * La entidad se almacena en la tabla
 * <p>Se persiste en la tabla {@code operacional.prf_sondaje}.</p>
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "prf_sondaje", schema = "operacional")
@Data
public class Sondaje implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "numero_interno", insertable = false, updatable = false)
    private Integer numeroInterno;

    @Column(name = "proyecto_id", nullable = false)
    private UUID proyectoId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "programa_sondaje_id")
    private ProgramaSondaje programaSondaje;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "collar_id")
    private Collar collar;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plataforma_id")
    private Plataforma plataforma;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "maquina_id")
    private Maquina maquina;

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "azimut_setup", nullable = false)
    private Double azimutSetup;

    @Column(name = "inclinacion_setup", nullable = false)
    private Double inclinacionSetup;

    @Column(name = "profundidad_actual")
    private Double profundidadActual = 0.0;

    @Column(name = "profundidad_final")
    private Double profundidadFinal;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(columnDefinition = "TEXT")
    private String notas;
}