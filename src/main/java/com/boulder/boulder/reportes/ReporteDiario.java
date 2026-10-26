package com.boulder.boulder.reportes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * Reporte diario consolidado de la campaña (pantalla P11, RF15).
 *
 * <p>Reemplaza el consolidado que hoy se arma a mano copiando del parte de
 * WhatsApp, del Excel de almacén y de los libros de logueo. Es inmutable: los
 * totales se calculan una vez, al construirlo con {@link #de}.</p>
 *
 * @param fecha          día del reporte
 * @param sondajes       una fila por sondaje con actividad ese día, ordenadas por código
 * @param ultimosDias    metros perforados en los siete días que terminan en {@code fecha}
 * @param totalPerforado metros perforados en el día
 * @param totalLogueado  metros con logueo geológico en el día
 * @param totalCajas     cajas de testigo recibidas en el almacén
 * @param totalMuestras  muestras registradas en el muestreo
 */
public record ReporteDiario(LocalDate fecha, List<FilaSondaje> sondajes, List<PuntoSerie> ultimosDias,
        BigDecimal totalPerforado, BigDecimal totalLogueado, int totalCajas, int totalMuestras) {

    /** Actividad de un sondaje en el día. */
    public record FilaSondaje(String codigo, BigDecimal perforado, BigDecimal logueado, int cajas, int muestras) {
    }

    /** Metros perforados en un día. */
    public record PuntoSerie(LocalDate fecha, BigDecimal metros) {
    }

    /**
     * Arma el reporte: ordena las filas por código y calcula los totales.
     *
     * @param fecha día del reporte
     * @param filas actividad por sondaje, en cualquier orden
     * @param serie metros perforados por día
     * @return el reporte inmutable
     */
    public static ReporteDiario de(LocalDate fecha, List<FilaSondaje> filas, List<PuntoSerie> serie) {
        List<FilaSondaje> ordenadas = filas.stream()
                .sorted(Comparator.comparing(FilaSondaje::codigo))
                .collect(ImmutableList.toImmutableList());
        return new ReporteDiario(fecha, ordenadas, ImmutableList.copyOf(serie),
                ordenadas.stream().map(FilaSondaje::perforado).reduce(BigDecimal.ZERO, BigDecimal::add),
                ordenadas.stream().map(FilaSondaje::logueado).reduce(BigDecimal.ZERO, BigDecimal::add),
                ordenadas.stream().mapToInt(FilaSondaje::cajas).sum(),
                ordenadas.stream().mapToInt(FilaSondaje::muestras).sum());
    }
}
