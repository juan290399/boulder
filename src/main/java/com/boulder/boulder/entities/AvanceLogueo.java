package com.boulder.boulder.entities;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Parte diario de avance de logueo (UC16).
 *
 * <p>
 * Registra el tramo de testigo descrito por el geólogo en un turno, separado
 * por tipo de logueo: un mismo sondaje puede llevar el geológico en 175 m y el
 * estructural en 204 m, y son frentes independientes.
 * </p>
 *
 * <p>Se persiste en la tabla {@code operacional.log_avance}.</p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "log_avance", schema = "operacional")
@Getter
@Setter
public class AvanceLogueo extends AvanceOperativo {

    /** geologico | geotecnico | estructural | rapido (lo restringe un CHECK). */
    @Column(name = "tipo_logueo", length = 20, nullable = false)
    private String tipoLogueo;

    @Column(nullable = false, precision = 9, scale = 2)
    private BigDecimal desde;

    @Column(nullable = false, precision = 9, scale = 2)
    private BigDecimal hasta;

    @Column(nullable = false, precision = 9, scale = 2)
    private BigDecimal avance;

    @Column(length = 80)
    private String geologo;

    @Column(name = "pct_completado", precision = 5, scale = 2)
    private BigDecimal pctCompletado;
}
