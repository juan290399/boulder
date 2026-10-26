package com.boulder.boulder.reportes;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boulder.boulder.reportes.ReporteDiario.PuntoSerie;

/**
 * Arma el reporte diario consolidado a partir de los mismos registros que usa la
 * operación: no hay que transcribir nada para generarlo.
 *
 * <p>El reloj se inyecta para que «hoy» se pueda fijar en las pruebas; en
 * producción es la hora de Lima.</p>
 */
@Service
public class ReporteDiarioService {

    private static final int DIAS_SERIE = 7;

    private final ReporteDiarioRepository repositorio;
    private final Clock reloj;

    public ReporteDiarioService(ReporteDiarioRepository repositorio, Clock reloj) {
        this.repositorio = repositorio;
        this.reloj = reloj;
    }

    /**
     * @param fecha día del reporte; {@code null} para hoy
     * @return el reporte del día con la serie de los últimos siete días
     * @throws IllegalArgumentException si la fecha es posterior a hoy
     */
    @Transactional(readOnly = true)
    public ReporteDiario generar(LocalDate fecha) {
        LocalDate hoy = LocalDate.now(reloj);
        LocalDate dia = fecha != null ? fecha : hoy;
        if (dia.isAfter(hoy)) {
            throw new IllegalArgumentException("No hay reporte de un día que todavía no pasó: " + dia);
        }
        LocalDate desde = dia.minusDays(DIAS_SERIE - 1L);
        Map<LocalDate, BigDecimal> perforado = repositorio.perforadoPorDia(desde, dia);
        List<PuntoSerie> serie = Stream.iterate(desde, d -> !d.isAfter(dia), d -> d.plusDays(1))
                .map(d -> new PuntoSerie(d, perforado.getOrDefault(d, BigDecimal.ZERO)))
                .toList();
        return ReporteDiario.de(dia, repositorio.porSondaje(dia), serie);
    }
}
