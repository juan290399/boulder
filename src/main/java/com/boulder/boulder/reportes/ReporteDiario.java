package com.boulder.boulder.reportes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
 * @param campania       resumen de la campaña hasta {@code fecha}, por sondaje
 * @param porMaquina     metros perforados en el día por máquina
 * @param mensual        metros perforados por sondaje y por mes hasta {@code fecha}
 */
public record ReporteDiario(LocalDate fecha, List<FilaSondaje> sondajes, List<PuntoSerie> ultimosDias,
        BigDecimal totalPerforado, BigDecimal totalLogueado, int totalCajas, int totalMuestras,
        List<FilaCampania> campania, List<FilaMaquina> porMaquina, Mensual mensual) {

    /** Metros perforados por una máquina en el día. */
    public record FilaMaquina(String maquina, BigDecimal metros) {
    }

    /** Metros perforados por un sondaje en un mes; es el dato crudo de la matriz mensual. */
    public record CeldaMensual(String codigo, YearMonth mes, BigDecimal metros) {
    }

    /** Fila de la matriz mensual: los metros de un sondaje en cada mes de {@link Mensual#meses()}. */
    public record FilaMensual(String codigo, List<BigDecimal> metros) {
    }

    /**
     * Matriz de metros perforados por sondaje y por mes.
     *
     * @param meses   meses con avance, en orden
     * @param filas   una fila por sondaje, ordenadas por código
     * @param totales total de cada mes
     */
    public record Mensual(List<YearMonth> meses, List<FilaMensual> filas, List<BigDecimal> totales) {

        static Mensual de(List<CeldaMensual> celdas) {
            List<YearMonth> meses = celdas.stream().map(CeldaMensual::mes).distinct().sorted().toList();
            Map<String, Map<YearMonth, BigDecimal>> porSondaje = new TreeMap<>();
            for (CeldaMensual c : celdas) {
                porSondaje.computeIfAbsent(c.codigo(), k -> new HashMap<>()).merge(c.mes(), c.metros(), BigDecimal::add);
            }
            List<FilaMensual> filas = porSondaje.entrySet().stream()
                    .map(e -> new FilaMensual(e.getKey(),
                            meses.stream().map(m -> e.getValue().getOrDefault(m, BigDecimal.ZERO)).toList()))
                    .toList();
            List<BigDecimal> totales = new ArrayList<>();
            for (int i = 0; i < meses.size(); i++) {
                final int col = i;
                totales.add(filas.stream().map(f -> f.metros().get(col)).reduce(BigDecimal.ZERO, BigDecimal::add));
            }
            return new Mensual(meses, filas, List.copyOf(totales));
        }
    }

    /** Un sondaje de la campaña: máquina, fechas de perforación, días, avance total y estado. */
    public record FilaCampania(String codigo, String maquina, LocalDate inicio, LocalDate fin, long dias,
            BigDecimal avance, String estado) {
    }

    /** Metros perforados en la campaña hasta la fecha del reporte. */
    public BigDecimal totalCampania() {
        return campania.stream().map(FilaCampania::avance).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

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
        return de(fecha, filas, serie, List.of());
    }

    /**
     * Igual que {@link #de(LocalDate, List, List)} con el resumen de campaña.
     *
     * @param campania sondajes de la campaña, en cualquier orden
     * @return el reporte inmutable
     */
    public static ReporteDiario de(LocalDate fecha, List<FilaSondaje> filas, List<PuntoSerie> serie,
                                   List<FilaCampania> campania) {
        return de(fecha, filas, serie, campania, List.of(), List.of());
    }

    /**
     * Igual que {@link #de(LocalDate, List, List, List)} con el avance por máquina y la matriz mensual.
     *
     * @param porMaquina metros del día por máquina, en cualquier orden
     * @param mensual    metros por sondaje y mes, en cualquier orden
     * @return el reporte inmutable
     */
    public static ReporteDiario de(LocalDate fecha, List<FilaSondaje> filas, List<PuntoSerie> serie,
                                   List<FilaCampania> campania, List<FilaMaquina> porMaquina,
                                   List<CeldaMensual> mensual) {
        List<FilaSondaje> ordenadas = filas.stream()
                .sorted(Comparator.comparing(FilaSondaje::codigo))
                .collect(ImmutableList.toImmutableList());
        return new ReporteDiario(fecha, ordenadas, ImmutableList.copyOf(serie),
                ordenadas.stream().map(FilaSondaje::perforado).reduce(BigDecimal.ZERO, BigDecimal::add),
                ordenadas.stream().map(FilaSondaje::logueado).reduce(BigDecimal.ZERO, BigDecimal::add),
                ordenadas.stream().mapToInt(FilaSondaje::cajas).sum(),
                ordenadas.stream().mapToInt(FilaSondaje::muestras).sum(),
                campania.stream().sorted(Comparator.comparing(FilaCampania::codigo))
                        .collect(ImmutableList.toImmutableList()),
                porMaquina.stream()
                        .sorted(Comparator.comparing(FilaMaquina::maquina, Comparator.nullsLast(Comparator.naturalOrder())))
                        .collect(ImmutableList.toImmutableList()),
                Mensual.de(mensual));
    }
}
