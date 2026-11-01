package com.boulder.boulder.reportes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.boulder.boulder.reportes.ReporteDiario.FilaSondaje;

/**
 * Reporte consolidado de un periodo (semanal, mensual o cualquier rango).
 *
 * @param desde          primer día, incluido
 * @param hasta          último día, incluido
 * @param sondajes       actividad acumulada por sondaje, ordenada por código
 * @param dias           una fila por día del periodo, también los días sin actividad
 * @param totalPerforado metros perforados en el periodo
 * @param totalLogueado  metros con logueo geológico
 * @param totalCajas     cajas recibidas en el almacén
 * @param totalMuestras  muestras registradas
 */
public record ReportePeriodo(LocalDate desde, LocalDate hasta, List<FilaSondaje> sondajes, List<FilaDia> dias,
        BigDecimal totalPerforado, BigDecimal totalLogueado, int totalCajas, int totalMuestras) {

    /** Actividad de la campaña en un día. */
    public record FilaDia(LocalDate fecha, BigDecimal perforado, BigDecimal logueado, int cajas, int muestras) {
    }

    public static ReportePeriodo de(LocalDate desde, LocalDate hasta, List<FilaSondaje> sondajes, List<FilaDia> dias) {
        return new ReportePeriodo(desde, hasta, List.of(), List.of(), BigDecimal.ZERO, BigDecimal.ZERO, 0, 0);
    }
}
