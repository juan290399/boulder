package com.boulder.boulder.reportes;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Arma el reporte de un periodo con los mismos registros que el reporte diario.
 */
@Service
public class ReportePeriodoService {

    /** Un trimestre: más que eso ya no es un reporte de seguimiento. */
    static final int MAX_DIAS = 92;
    private static final int DIAS_POR_DEFECTO = 7;

    private final ReporteDiarioRepository repositorio;
    private final Clock reloj;

    public ReportePeriodoService(ReporteDiarioRepository repositorio, Clock reloj) {
        this.repositorio = repositorio;
        this.reloj = reloj;
    }

    /**
     * @param desde primer día; {@code null} para seis días antes de {@code hasta}
     * @param hasta último día; {@code null} para hoy
     * @return el reporte del periodo
     * @throws IllegalArgumentException si el periodo está invertido, termina en el futuro o pasa de 92 días
     */
    @Transactional(readOnly = true)
    public ReportePeriodo generar(LocalDate desde, LocalDate hasta) {
        LocalDate hoy = LocalDate.now(reloj);
        LocalDate fin = hasta != null ? hasta : hoy;
        LocalDate inicio = desde != null ? desde : fin.minusDays(DIAS_POR_DEFECTO - 1L);
        if (inicio.isAfter(fin)) {
            throw new IllegalArgumentException("El periodo empieza después de terminar: " + inicio + " > " + fin);
        }
        if (fin.isAfter(hoy)) {
            throw new IllegalArgumentException("El periodo no puede terminar en el futuro: " + fin);
        }
        if (ChronoUnit.DAYS.between(inicio, fin) + 1 > MAX_DIAS) {
            throw new IllegalArgumentException("El periodo pasa de " + MAX_DIAS + " días");
        }
        return ReportePeriodo.de(inicio, fin, repositorio.porSondaje(inicio, fin), repositorio.porDia(inicio, fin),
                repositorio.desvios(inicio, fin));
    }
}
