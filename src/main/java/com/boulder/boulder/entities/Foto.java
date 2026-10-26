package com.boulder.boulder.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Una foto de cajas de testigo.
 *
 * <p>
 * Solo los metadatos: el archivo vive en el almacén de fotos y aquí queda su
 * clave. El sondaje va como identificador suelto y no como relación JPA,
 * porque la galería nunca necesita el sondaje entero y así la entidad se
 * serializa sin arrastrar una carga perezosa.
 * </p>
 */
@Entity
@Table(name = "fot_foto", schema = "operacional")
@Getter
@Setter
public class Foto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private UUID id;

    @Column(name = "numero_interno", insertable = false, updatable = false)
    private Integer numeroInterno;

    @Column(name = "sondaje_id", nullable = false)
    private UUID sondajeId;

    @Column(name = "caja_desde")
    private Integer cajaDesde;

    @Column(name = "caja_hasta")
    private Integer cajaHasta;

    @Column(precision = 9, scale = 2)
    private BigDecimal desde;

    @Column(precision = 9, scale = 2)
    private BigDecimal hasta;

    /** {@code seco} o {@code humedo}: se fotografía el testigo de las dos formas. */
    @Column(length = 10)
    private String condicion;

    @Column(name = "tomada_en")
    private OffsetDateTime tomadaEn;

    @Column(length = 80)
    private String autor;

    @Column(length = 64)
    private String dispositivo;

    @Column(name = "archivo_original", length = 255)
    private String archivoOriginal;

    @Column(name = "tipo_mime", length = 40, nullable = false)
    private String tipoMime;

    @Column(nullable = false)
    private Long bytes;

    private Integer ancho;

    private Integer alto;

    @Column(length = 64, nullable = false)
    private String sha256;

    @Column(name = "clave_almacen", length = 255, nullable = false)
    private String claveAlmacen;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "eliminado_en")
    private OffsetDateTime eliminadoEn;

    @Column(name = "creado_en", insertable = false, updatable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "actualizado_en", insertable = false, updatable = false)
    private OffsetDateTime actualizadoEn;
}
