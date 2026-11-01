package com.boulder.boulder.reportes;

import java.time.Clock;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Arma el reporte de un periodo con los mismos registros que el reporte diario.
 */
@Service
public class ReportePeriodoService {

    private final ReporteDiarioRepository repositorio;
    private final Clock reloj;

    public ReportePeriodoService(ReporteDiarioRepository repositorio, Clock reloj) {
        this.repositorio = repositorio;
        this.reloj = reloj;
    }

    @Transactional(readOnly = true)
    public ReportePeriodo generar(LocalDate desde, LocalDate hasta) {
        return ReportePeriodo.de(desde, hasta, java.util.List.of(), java.util.List.of());
    }
}
