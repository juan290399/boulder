package com.boulder.boulder.entities;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Parte de corte de testigo para muestreo geoquímico (UC18).
 *
 * <p>
 * Es el paso irreversible del flujo: una vez cortado el testigo, la mitad que
 * se va a análisis no vuelve. Por eso el corte solo se autoriza sobre tramo ya
 * logueado y fotografiado, y el parte deja constancia de hasta dónde se llegó.
 * </p>
 *
 * <p>Se persiste en la tabla {@code operacional.crt_muestra}.</p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "crt_muestra", schema = "operacional")
@Getter
@Setter
public class CorteMuestra extends AvanceOperativo {

    @Column(length = 80)
    private String responsable;

    @Column(nullable = false, precision = 9, scale = 2)
    private BigDecimal desde;

    @Column(nullable = false, precision = 9, scale = 2)
    private BigDecimal hasta;

    /** Diámetro de la línea de perforación: HQ, HQ3, NQ... */
    @Column(name = "tipo_linea", length = 10)
    private String tipoLinea;

    @Column(name = "metraje_estimado", precision = 9, scale = 2)
    private BigDecimal metrajeEstimado;

    @Column(name = "nro_muestras")
    private Integer nroMuestras;
}
