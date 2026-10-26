package com.boulder.boulder.entities;

import java.io.Serializable;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
 * Representa una plataforma de perforación dentro de un proyecto.
 *
 * <p>Una plataforma corresponde al área acondicionada para la ejecución
 * de uno o más sondajes, incluyendo su ubicación geográfica, dimensiones
 * y características operacionales.</p>
 *
 * <p>Se persiste en la tabla {@code operacional.prf_plataforma}.</p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "prf_plataforma", schema = "operacional")
@Data
public class Plataforma implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "numero_interno", insertable = false, updatable = false)
    private Integer numeroInterno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id", referencedColumnName = "id", nullable = false)
    private Proyecto proyecto;

    // Columna char(8) en la base: CHAR, no VARCHAR (ver Proyecto.codigo).
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(nullable = false, unique = true, length = 8)
    private String codigo;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(nullable = false)
    private Double este;

    @Column(nullable = false)
    private Double norte;

    @Column(nullable = false)
    private Double elevacion;

    @Column(name = "eje_mayor")
    private Double ejeMayor;

    @Column(name = "eje_menor")
    private Double ejeMinor;

    @Column(name = "eje_z")
    private Double ejeZ;

    private Double area;

    @Column(name = "numero_pozas")
    private Short numeroPozas = 0;

    @Column(length = 30)
    private String estado;

    @Column(length = 50)
    private String tipo;

    @Column(columnDefinition = "TEXT")
    private String ubicacion;

    @Column(columnDefinition = "TEXT")
    private String notas;
}