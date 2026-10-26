package com.boulder.boulder.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Medida estructural de un sondaje.
 *
 * <p>A diferencia de las otras cuatro plantillas <b>no es un intervalo</b>:
 * es una medida puntual a una {@code profundidad}, con los ángulos alfa y
 * beta respecto al eje del testigo. {@code desde} y {@code hasta} existen y
 * admiten nulo, para la estructura que sí ocupa un tramo.</p>
 *
 * <p>Se persiste en la tabla {@code logueo.iestructural}.</p>
 *
 * <p><b>Las claves foráneas a {@code geologia} se guardan como UUID sueltos</b>,
 * sin {@code @ManyToOne}. Son 8 referencias a catálogos de solo lectura que
 * el cliente ya tiene descargados enteros: mapearlas como relaciones traería
 * veintiuna entidades más, una consulta por celda al serializar y nada a cambio,
 * porque lo que viaja por la API es el id, no el nombre.</p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "iestructural", schema = "logueo")
@Data
public class Estructural implements Serializable, RegistroLogueo {

    @Id
    private UUID id;

    // Lo asigna una secuencia de PostgreSQL; el cliente nunca lo manda.
    @Column(name = "numero_interno", insertable = false, updatable = false)
    private Integer numeroInterno;

    @Column(name = "sondaje_id", nullable = false)
    private UUID sondajeId;

    @Column(name = "desde")
    private BigDecimal desde;

    @Column(name = "hasta")
    private BigDecimal hasta;

    @Column(name = "profundidad", nullable = false)
    private BigDecimal profundidad;

    @Column(name = "longitud")
    private BigDecimal longitud;

    @Column(name = "alpha_angle")
    private BigDecimal alphaAngle;

    @Column(name = "beta_angle", nullable = false)
    private BigDecimal betaAngle;

    @Column(name = "dip")
    private BigDecimal dip;

    @Column(name = "tipo_estructura_id", nullable = false)
    private UUID tipoEstructuraId;

    @Column(name = "tipo_venilla_id")
    private UUID tipoVenillaId;

    @Column(name = "espesor_id")
    private UUID espesorId;

    @Column(name = "forma_id")
    private UUID formaId;

    @Column(name = "rugosidad_id")
    private UUID rugosidadId;

    @Column(name = "relleno_1_id")
    private UUID relleno1Id;

    @Column(name = "relleno_2_id")
    private UUID relleno2Id;

    @Column(name = "litologia_id", nullable = false)
    private UUID litologiaId;

    @Column(name = "orientacion")
    private String orientacion;

    @Column(name = "comentarios")
    private String comentarios;

    @Column(name = "fecha")
    private LocalDate fecha;

    @Column(name = "geologo")
    private String geologo;

    /** Borrado lógico. NULL = vigente; con fecha, el tablet lo borró. */
    @Column(name = "eliminado_en")
    private OffsetDateTime eliminadoEn;

    /** Último cambio en el servidor; lo mantiene un trigger de la base. */
    @Column(name = "actualizado_en", insertable = false, updatable = false)
    private OffsetDateTime actualizadoEn;
}
