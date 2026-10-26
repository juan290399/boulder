package com.boulder.boulder.repositories;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Proyección de la consulta del frente de avance por sondaje.
 *
 * <p>
 * Spring Data la rellena a partir de las etiquetas de columna de la consulta
 * nativa. Los alias van entrecomillados en el SQL porque PostgreSQL pasa a
 * minúsculas todo identificador sin comillas, y entonces {@code sondajeCodigo}
 * llegaría como {@code sondajecodigo} y no casaría con el captador.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
public interface FrenteAvanceProjection {

    UUID getSondajeId();

    String getSondajeCodigo();

    String getSondajeNombre();

    BigDecimal getProfundidadActual();

    BigDecimal getProfundidadFinal();

    LocalDate getFechaInicio();

    LocalDate getFechaFin();

    BigDecimal getPerforado();

    BigDecimal getRecepcionado();

    BigDecimal getLogueado();

    BigDecimal getLogueadoGeomecanico();

    BigDecimal getLogueadoEstructural();

    BigDecimal getFotografiado();

    BigDecimal getCortado();

    BigDecimal getMuestreado();

    BigDecimal getDensidadCortada();

    Long getMuestrasEmbolsadas();

    Long getCajasRecibidas();

    LocalDate getUltimaActividad();
}
