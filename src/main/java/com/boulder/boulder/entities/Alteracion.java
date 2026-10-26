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
 * Tramo de alteración de un sondaje.
 *
 * <p>Plantilla ancha: cinco ranuras repetidas de mineral, estilo, porcentaje
 * y densidad de vetillas, más dos tipos de alteración con su intensidad. La
 * repetición es del modelo de datos, no un descuido: el geólogo describe
 * hasta cinco minerales de alteración por tramo.</p>
 *
 * <p>Se persiste en la tabla {@code logueo.ialteracion}.</p>
 *
 * <p><b>Las claves foráneas a {@code geologia} se guardan como UUID sueltos</b>,
 * sin {@code @ManyToOne}. Son 17 referencias a catálogos de solo lectura que
 * el cliente ya tiene descargados enteros: mapearlas como relaciones traería
 * veintiuna entidades más, una consulta por celda al serializar y nada a cambio,
 * porque lo que viaja por la API es el id, no el nombre.</p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "ialteracion", schema = "logueo")
@Data
public class Alteracion implements Serializable, RegistroLogueo {

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

    @Column(name = "longitud")
    private BigDecimal longitud;

    @Column(name = "alt_min_1")
    private UUID altMin1;

    @Column(name = "alt_estilo_1")
    private UUID altEstilo1;

    @Column(name = "alt_pc_1")
    private BigDecimal altPc1;

    @Column(name = "vetillas_m_1")
    private Short vetillasM1;

    @Column(name = "alt_min_2")
    private UUID altMin2;

    @Column(name = "alt_estilo_2")
    private UUID altEstilo2;

    @Column(name = "alt_pc_2")
    private BigDecimal altPc2;

    @Column(name = "vetillas_m_2")
    private Short vetillasM2;

    @Column(name = "alt_min_3")
    private UUID altMin3;

    @Column(name = "alt_estilo_3")
    private UUID altEstilo3;

    @Column(name = "alt_pc_3")
    private BigDecimal altPc3;

    @Column(name = "vetillas_m_3")
    private Short vetillasM3;

    @Column(name = "alt_min_4")
    private UUID altMin4;

    @Column(name = "alt_estilo_4")
    private UUID altEstilo4;

    @Column(name = "alt_pc_4")
    private BigDecimal altPc4;

    @Column(name = "vetillas_m_4")
    private Short vetillasM4;

    @Column(name = "alt_min_5")
    private UUID altMin5;

    @Column(name = "alt_estilo_5")
    private UUID altEstilo5;

    @Column(name = "alt_pc_5")
    private BigDecimal altPc5;

    @Column(name = "vetillas_m_5")
    private Short vetillasM5;

    @Column(name = "alt_tipo_1", nullable = false)
    private UUID altTipo1;

    @Column(name = "alt_intensidad_1")
    private UUID altIntensidad1;

    @Column(name = "alt_tipo_2")
    private UUID altTipo2;

    @Column(name = "alt_intensidad_2")
    private UUID altIntensidad2;

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
