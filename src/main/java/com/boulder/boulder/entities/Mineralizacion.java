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
 * Tramo de mineralización de un sondaje.
 *
 * <p>Cuatro ranuras de mineral con su porcentaje y distribución, y las leyes
 * de cobre y molibdeno que se derivan de ellas. <b>Las leyes no las teclea
 * nadie</b>: {@code cu_m_pc_N} es la fracción de cobre del mineral y
 * {@code cu_pc_N} lo que ese mineral aporta al tramo; el cliente las calcula
 * y las manda ya resueltas, igual que hacía la hoja de cálculo a la que
 * sustituye.</p>
 *
 * <p>Se persiste en la tabla {@code logueo.imineralizacion}.</p>
 *
 * <p><b>Las claves foráneas a {@code geologia} se guardan como UUID sueltos</b>,
 * sin {@code @ManyToOne}. Son 10 referencias a catálogos de solo lectura que
 * el cliente ya tiene descargados enteros: mapearlas como relaciones traería
 * veintiuna entidades más, una consulta por celda al serializar y nada a cambio,
 * porque lo que viaja por la API es el id, no el nombre.</p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "imineralizacion", schema = "logueo")
@Data
public class Mineralizacion implements Serializable, RegistroLogueo {

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

    @Column(name = "min_zona_id", nullable = false)
    private UUID minZonaId;

    @Column(name = "min_1")
    private UUID min1;

    @Column(name = "min_pc_1")
    private BigDecimal minPc1;

    @Column(name = "min_dist_1")
    private UUID minDist1;

    @Column(name = "cu_m_pc_1")
    private Double cuMPc1;

    @Column(name = "cu_pc_1")
    private Double cuPc1;

    @Column(name = "mo_m_pc_1")
    private Double moMPc1;

    @Column(name = "mo_pc_1")
    private Double moPc1;

    @Column(name = "min_2")
    private UUID min2;

    @Column(name = "min_pc_2")
    private BigDecimal minPc2;

    @Column(name = "min_dist_2")
    private UUID minDist2;

    @Column(name = "cu_m_pc_2")
    private Double cuMPc2;

    @Column(name = "cu_pc_2")
    private Double cuPc2;

    @Column(name = "mo_m_pc_2")
    private Double moMPc2;

    @Column(name = "mo_pc_2")
    private Double moPc2;

    @Column(name = "min_3")
    private UUID min3;

    @Column(name = "min_pc_3")
    private BigDecimal minPc3;

    @Column(name = "min_dist_3")
    private UUID minDist3;

    @Column(name = "cu_m_pc_3")
    private Double cuMPc3;

    @Column(name = "cu_pc_3")
    private Double cuPc3;

    @Column(name = "mo_m_pc_3")
    private Double moMPc3;

    @Column(name = "mo_pc_3")
    private Double moPc3;

    @Column(name = "min_4")
    private UUID min4;

    @Column(name = "min_pc_4")
    private BigDecimal minPc4;

    @Column(name = "min_dist_4")
    private UUID minDist4;

    @Column(name = "cu_m_pc_4")
    private Double cuMPc4;

    @Column(name = "cu_pc_4")
    private Double cuPc4;

    @Column(name = "mo_m_pc_4")
    private Double moMPc4;

    @Column(name = "mo_pc_4")
    private Double moPc4;

    @Column(name = "cu_pct")
    private Double cuPct;

    @Column(name = "mo_pct")
    private Double moPct;

    @Column(name = "descripcion")
    private String descripcion;

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
