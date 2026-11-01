package com.boulder.boulder.reportes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.boulder.boulder.reportes.ReporteDiario.FilaSondaje;
import com.google.common.collect.ImmutableList;

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

        static FilaDia vacia(LocalDate fecha) {
            return new FilaDia(fecha, BigDecimal.ZERO, BigDecimal.ZERO, 0, 0);
        }
    }

    /**
     * Ordena los sondajes por código, completa los días sin actividad y calcula los totales.
     *
     * @param desde primer día, incluido
     * @param hasta último día, incluido
     * @param sondajes actividad por sondaje, en cualquier orden
     * @param dias actividad por día; los días que faltan quedan en cero
     * @return el reporte inmutable
     */
    public static ReportePeriodo de(LocalDate desde, LocalDate hasta, List<FilaSondaje> sondajes, List<FilaDia> dias) {
        List<FilaSondaje> ordenadas = sondajes.stream()
                .sorted(Comparator.comparing(FilaSondaje::codigo))
                .collect(ImmutableList.toImmutableList());
        Map<LocalDate, FilaDia> porFecha = dias.stream()
                .collect(Collectors.toMap(FilaDia::fecha, Function.identity(), (a, b) -> a));
        List<FilaDia> completos = Stream.iterate(desde, d -> !d.isAfter(hasta), d -> d.plusDays(1))
                .map(d -> porFecha.getOrDefault(d, FilaDia.vacia(d)))
                .collect(ImmutableList.toImmutableList());
        return new ReportePeriodo(desde, hasta, ordenadas, completos,
                ordenadas.stream().map(FilaSondaje::perforado).reduce(BigDecimal.ZERO, BigDecimal::add),
                ordenadas.stream().map(FilaSondaje::logueado).reduce(BigDecimal.ZERO, BigDecimal::add),
                ordenadas.stream().mapToInt(FilaSondaje::cajas).sum(),
                ordenadas.stream().mapToInt(FilaSondaje::muestras).sum());
    }
}
