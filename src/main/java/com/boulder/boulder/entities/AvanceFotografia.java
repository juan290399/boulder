package com.boulder.boulder.entities;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Parte diario de registro fotográfico de cajas (CU13).
 *
 * <p>
 * Las cajas se fotografían dos veces, en seco y en húmedo, y el campo
 * {@code secoHumedo} distingue de cuál de las dos pasadas es el parte.
 * </p>
 *
 * <p>Se persiste en la tabla {@code operacional.fot_avance}.</p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "fot_avance", schema = "operacional")
@Getter
@Setter
public class AvanceFotografia extends AvanceOperativo {

    @Column(length = 80)
    private String responsable;

    @Column(name = "box_desde")
    private Integer boxDesde;

    @Column(name = "box_hasta")
    private Integer boxHasta;

    @Column(name = "total_cajas")
    private Integer totalCajas;

    @Column(name = "marcado_desde", precision = 9, scale = 2)
    private BigDecimal marcadoDesde;

    @Column(name = "marcado_hasta", precision = 9, scale = 2)
    private BigDecimal marcadoHasta;

    @Column(name = "total_metraje", precision = 9, scale = 2)
    private BigDecimal totalMetraje;

    /** seco | humedo (lo restringe un CHECK). */
    @Column(name = "seco_humedo", length = 10)
    private String secoHumedo;

    @Column(name = "nro_fotos")
    private Integer nroFotos;

    @Column(name = "ruta_archivos", columnDefinition = "TEXT")
    private String rutaArchivos;
}
