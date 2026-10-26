package com.boulder.boulder.entities;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Parte diario de muestreo geoquímico (CU17).
 *
 * <p>
 * Cuenta el embolsado y el agrupamiento en costales y lotes, que es lo que
 * después viaja al laboratorio. Las muestras cortadas y las embolsadas se
 * llevan por separado porque entre una cosa y la otra suele haber un turno de
 * diferencia.
 * </p>
 *
 * <p>Se persiste en la tabla {@code operacional.mst_avance}.</p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "mst_avance", schema = "operacional")
@Getter
@Setter
public class AvanceMuestreo extends AvanceOperativo {

    @Column(length = 80)
    private String responsable;

    /** en_proceso | finalizado (lo restringe un CHECK). */
    @Column(name = "estado_muestreo", length = 15, nullable = false)
    private String estadoMuestreo;

    @Column(precision = 9, scale = 2)
    private BigDecimal desde;

    @Column(precision = 9, scale = 2)
    private BigDecimal hasta;

    @Column(precision = 9, scale = 2)
    private BigDecimal avance;

    @Column(name = "nro_muestras")
    private Integer nroMuestras;

    @Column(name = "nro_muestras_embolsadas")
    private Integer nroMuestrasEmbolsadas;

    @Column(name = "nro_costales")
    private Integer nroCostales;

    @Column(name = "nro_lotes")
    private Integer nroLotes;
}
