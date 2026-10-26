package com.boulder.boulder.entities;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

import org.hibernate.annotations.Generated;

import lombok.Getter;
import lombok.Setter;

/**
 * Cabecera común a todos los partes de avance operativo (UC16 a UC20).
 *
 * <p>
 * Las siete tablas del flujo posterior a la perforación —logueo, recepción de
 * cajas, fotografía, corte geoquímico, muestreo, corte de densidad y estufa—
 * comparten exactamente los mismos ocho campos de cabecera. Se declaran una
 * sola vez aquí para que un cambio en el criterio de identificación del parte
 * no haya que replicarlo siete veces.
 * </p>
 *
 * <p>
 * El sondaje se puede indicar por relación o por código suelto. No es un
 * descuido: el {@code CHECK} de cada tabla exige uno de los dos, y hay partes
 * legítimos que no corresponden a un único sondaje —un lote de estufa que
 * mezcla testigos de varios se registra con el código {@code VARIOS} y sin
 * relación.
 * </p>
 *
 * <p>
 * Se usa {@code @Getter}/{@code @Setter} en vez de {@code @Data} a propósito:
 * el {@code toString()} que genera {@code @Data} recorre {@link #sondaje}, que
 * es perezoso, y revienta con {@code LazyInitializationException} en cuanto
 * alguien loguea la entidad fuera de la transacción.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@MappedSuperclass
@Getter
@Setter
public abstract class AvanceOperativo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Correlativo legible que asigna la secuencia de la tabla; la base manda. */
    @Generated
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

    @Column(length = 20, nullable = false)
    private String estado = "registrado";

    @Column(columnDefinition = "TEXT")
    private String observaciones;
}
