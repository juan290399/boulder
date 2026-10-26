package com.boulder.boulder.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
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
 * Evento de medición de desviación de un sondaje.
 *
 * <p>
 * Complementa {@link Survey} (trayectoria aceptada) con la metadata del acto de
 * medir y la desviación respecto al plan, para seguimiento en tiempo real y
 * analítica (qué perforistas/máquinas desvían más, tendencias por profundidad).
 * </p>
 *
 * <p>Se persiste en la tabla {@code operacional.prf_survey_medicion}.</p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "prf_survey_medicion", schema = "operacional")
@Data
public class SurveyMedicion implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "numero_interno", insertable = false, updatable = false)
    private Integer numeroInterno;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sondaje_id", nullable = false)
    private Sondaje sondaje;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maquina_id")
    private Maquina maquina;

    /**
     * El perforista de ese turno. La columna conserva el nombre
     * {@code perforista_id} y su significado: no es "un empleado cualquiera",
     * es quien operaba la máquina. Lo que cambió es la tabla a la que apunta,
     * ahora {@code base.empleado}.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "perforista_id")
    private Empleado perforista;

    @Column(nullable = false, precision = 9, scale = 2)
    private BigDecimal profundidad;

    @Column(name = "nro_medicion")
    private Short nroMedicion;

    @Column(name = "fecha_medicion", nullable = false)
    private LocalDate fechaMedicion;

    @Column(name = "fecha_hora")
    private OffsetDateTime fechaHora;

    @Column(length = 80)
    private String responsable;

    @Column(length = 50)
    private String metodo;

    @Column(length = 50)
    private String instrumento;

    @Column(name = "azimut_medido")
    private Double azimutMedido;

    @Column(name = "inclinacion_medida")
    private Double inclinacionMedida;

    @Column(name = "azimut_plan")
    private Double azimutPlan;

    @Column(name = "inclinacion_plan")
    private Double inclinacionPlan;

    @Column(name = "desv_azimut")
    private Double desvAzimut;

    @Column(name = "desv_azimut_sentido", length = 10)
    private String desvAzimutSentido;

    @Column(name = "desv_inclinacion")
    private Double desvInclinacion;

    @Column(name = "desv_inclinacion_sentido", length = 10)
    private String desvInclinacionSentido;

    @Column(name = "desv_metros")
    private Double desvMetros;

    @Column(name = "desv_total")
    private Double desvTotal;

    @Column(name = "es_planificada", nullable = false)
    private Boolean esPlanificada = Boolean.FALSE;

    @Column(length = 20)
    private String decision;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    /**
     * Por qué se desvió: la lectura geológica del desvío, no el número.
     *
     * <p>
     * Los tres campos que siguen no son adorno. El reporte que hoy circula por
     * WhatsApp lleva el número, pero lo que la gente lee de ese mensaje es la
     * instrucción y a quién le toca. Sin ellos la pantalla informa y el grupo
     * de WhatsApp sigue siendo la fuente de verdad de lo que hay que hacer.
     * </p>
     */
    @Column(columnDefinition = "TEXT")
    private String interpretacion;

    /** Qué hacer: la indicación operativa que se le da a la máquina. */
    @Column(columnDefinition = "TEXT")
    private String instruccion;

    @Column(name = "responsable_seguimiento", length = 80)
    private String responsableSeguimiento;

    /** sin_seguimiento | pendiente | en_curso | cerrado (lo restringe un CHECK). */
    @Column(name = "estado_seguimiento", length = 20, nullable = false)
    private String estadoSeguimiento = "sin_seguimiento";
}
