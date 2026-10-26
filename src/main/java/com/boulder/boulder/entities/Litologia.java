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
 * Tramo de litología de un sondaje: el intervalo de testigo descrito
 * como una sola roca.
 *
 * <p>Se persiste en la tabla {@code logueo.ilitologia}.</p>
 *
 * <p><b>Las claves foráneas a {@code geologia} se guardan como UUID sueltos</b>,
 * sin {@code @ManyToOne}. Son 23 referencias a catálogos de solo lectura que
 * el cliente ya tiene descargados enteros: mapearlas como relaciones traería
 * veintiuna entidades más, una consulta por celda al serializar y nada a cambio,
 * porque lo que viaja por la API es el id, no el nombre.</p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "ilitologia", schema = "logueo")
@Data
public class Litologia implements Serializable, RegistroLogueo {

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

    @Column(name = "tipo_material_id", nullable = false)
    private UUID tipoMaterialId;

    @Column(name = "color_id")
    private UUID colorId;

    @Column(name = "intensidad_color_id")
    private UUID intensidadColorId;

    @Column(name = "grupo_litologico_id", nullable = false)
    private UUID grupoLitologicoId;

    @Column(name = "lit_diccionario_id", nullable = false)
    private UUID litDiccionarioId;

    @Column(name = "tipo_subroca")
    private String tipoSubroca;

    @Column(name = "descripcion_roca")
    private String descripcionRoca;

    @Column(name = "tamanio_grano_id")
    private UUID tamanioGranoId;

    @Column(name = "lit_textura_id")
    private UUID litTexturaId;

    @Column(name = "bx_pc_clasto")
    private Short bxPcClasto;

    @Column(name = "bx_forma_id")
    private UUID bxFormaId;

    @Column(name = "bx_pc_matriz")
    private Short bxPcMatriz;

    @Column(name = "bx_tipo_matriz_id")
    private UUID bxTipoMatrizId;

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
