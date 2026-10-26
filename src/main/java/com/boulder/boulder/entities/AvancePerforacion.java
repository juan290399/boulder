package com.boulder.boulder.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Parte diario de avance de perforación (UC15).
 *
 * <p>
 * Registra el metraje perforado por turno en un sondaje, los parámetros
 * operativos (casing, retorno, línea) y la dotación de máquina asociada.
 * </p>
 *
 * <p>Se persiste en la tabla {@code operacional.prf_avance}.</p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "prf_avance", schema = "operacional")
@Data
public class AvancePerforacion implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "numero_interno", insertable = false, updatable = false)
    private Integer numeroInterno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sondaje_id")
    private Sondaje sondaje;

    @Column(name = "sondaje_codigo", length = 30)
    private String sondajeCodigo;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(length = 10)
    private String turno;

    @Column(length = 80)
    private String responsable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maquina_id")
    private Maquina maquina;

    @Column(name = "maquina_codigo", length = 20)
    private String maquinaCodigo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plataforma_id")
    private Plataforma plataforma;

    @Column(name = "plataforma_codigo", length = 20)
    private String plataformaCodigo;

    @Column(nullable = false, precision = 9, scale = 2)
    private BigDecimal desde;

    @Column(nullable = false, precision = 9, scale = 2)
    private BigDecimal hasta;

    @Column(nullable = false, precision = 9, scale = 2)
    private BigDecimal avance;

    @Column(name = "casing_tipo", length = 15)
    private String casingTipo;

    @Column(name = "casing_desde", precision = 9, scale = 2)
    private BigDecimal casingDesde;

    @Column(name = "casing_hasta", precision = 9, scale = 2)
    private BigDecimal casingHasta;

    @Column(name = "retorno_pct", precision = 5, scale = 2)
    private BigDecimal retornoPct;

    @Column(length = 10)
    private String linea;

    @Column(columnDefinition = "TEXT")
    private String comentarios;

    @Column(length = 20, nullable = false)
    private String estado = "registrado";

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @OneToMany(mappedBy = "avance", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AvancePersonal> personal = new ArrayList<>();
}
