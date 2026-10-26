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
 * Tramo de logueo rápido: el testigo entero descrito con lo mínimo.
 *
 * <p>Reúne roca, alteración, mineralización y tres leyes estimadas en un
 * solo intervalo. No sustituye a las otras cuatro plantillas; sirve para la
 * pasada rápida previa y para los tramos estériles. Aquí {@code pc_cu} y
 * {@code pc_mo} <b>sí</b> son estimaciones directas del geólogo: la tabla
 * guarda los minerales sin cuantificar, así que no hay de dónde derivarlas.</p>
 *
 * <p>Se persiste en la tabla {@code logueo.iquicklog}.</p>
 *
 * <p><b>Las claves foráneas a {@code geologia} se guardan como UUID sueltos</b>,
 * sin {@code @ManyToOne}. Son 16 referencias a catálogos de solo lectura que
 * el cliente ya tiene descargados enteros: mapearlas como relaciones traería
 * veintiuna entidades más, una consulta por celda al serializar y nada a cambio,
 * porque lo que viaja por la API es el id, no el nombre.</p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "iquicklog", schema = "logueo")
@Data
public class Quicklog implements Serializable, RegistroLogueo {

    @Id
    private UUID id;

    // Lo asigna una secuencia de PostgreSQL; el cliente nunca lo manda.
    @Column(name = "numero_interno", insertable = false, updatable = false)
    private Integer numeroInterno;

    @Column(name = "sondaje_id", nullable = false)
    private UUID sondajeId;

    @Column(name = "desde", nullable = false)
    private BigDecimal desde;

    @Column(name = "hasta", nullable = false)
    private BigDecimal hasta;

    @Column(name = "longitud", nullable = false)
    private BigDecimal longitud;

    @Column(name = "lito_id", nullable = false)
    private UUID litoId;

    @Column(name = "alt_tipo_1_id")
    private UUID altTipo1Id;

    @Column(name = "alt_intensidad_1_id")
    private UUID altIntensidad1Id;

    @Column(name = "alt_tipo_2_id")
    private UUID altTipo2Id;

    @Column(name = "alt_intensidad_2_id")
    private UUID altIntensidad2Id;

    @Column(name = "alt_aso_1_id")
    private UUID altAso1Id;

    @Column(name = "alt_aso_2_id")
    private UUID altAso2Id;

    @Column(name = "alt_aso_3_id")
    private UUID altAso3Id;

    @Column(name = "min_tipo_1_id")
    private UUID minTipo1Id;

    @Column(name = "min_tipo_2_id")
    private UUID minTipo2Id;

    @Column(name = "min_tipo_3_id")
    private UUID minTipo3Id;

    @Column(name = "min_estilo_1_id")
    private UUID minEstilo1Id;

    @Column(name = "min_estilo_2_id")
    private UUID minEstilo2Id;

    @Column(name = "min_estilo_3_id")
    private UUID minEstilo3Id;

    @Column(name = "pc_py")
    private BigDecimal pcPy;

    @Column(name = "pc_cu")
    private BigDecimal pcCu;

    @Column(name = "pc_mo")
    private BigDecimal pcMo;

    @Column(name = "min_zona_id")
    private UUID minZonaId;

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
