package com.boulder.boulder.entities;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Parte de procesamiento de densidad en estufa (CU16).
 *
 * <p>
 * El ensayo pasa por cinco etapas encadenadas —corte, dos secados, pesado y
 * cálculo— y cada una genera su propio parte sobre el mismo juego de muestras.
 * Las bandejas de estufa mezclan testigos de varios sondajes, así que este es
 * el parte donde el sondaje puede quedar sin relación y registrarse como
 * {@code VARIOS}.
 * </p>
 *
 * <p>Se persiste en la tabla {@code operacional.dns_avance}.</p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "dns_avance", schema = "operacional")
@Getter
@Setter
public class AvanceDensidad extends AvanceOperativo {

    @Column(length = 80)
    private String responsable;

    /** corte | secado_1 | secado_2 | pesado | calculo (lo restringe un CHECK). */
    @Column(length = 20, nullable = false)
    private String etapa;

    @Column(name = "nro_muestras", nullable = false)
    private Integer nroMuestras;

    @Column(name = "temperatura_c", precision = 5, scale = 1)
    private BigDecimal temperaturaC;

    @Column(name = "tipo_linea", length = 10)
    private String tipoLinea;

    @Column(precision = 9, scale = 2)
    private BigDecimal desde;

    @Column(precision = 9, scale = 2)
    private BigDecimal hasta;
}
