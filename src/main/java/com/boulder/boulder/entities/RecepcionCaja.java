package com.boulder.boulder.entities;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Recepción de cajas de campo en el almacén de testigos (apoyo a UC16).
 *
 * <p>
 * Es el eslabón entre la perforación y el logueo: hasta que las cajas no se
 * reciben y se contrastan sus marcas de profundidad, el geólogo no tiene qué
 * describir. El metraje recibido casi nunca coincide con el perforado, y esa
 * diferencia es justamente lo que el parte deja registrado.
 * </p>
 *
 * <p>Se persiste en la tabla {@code operacional.log_recepcion_caja}.</p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "log_recepcion_caja", schema = "operacional")
@Getter
@Setter
public class RecepcionCaja extends AvanceOperativo {

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
}
