package com.boulder.boulder.reportes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Reporte diario consolidado. */
public record ReporteDiario(LocalDate fecha, List<FilaSondaje> sondajes, List<PuntoSerie> ultimosDias,
        BigDecimal totalPerforado, BigDecimal totalLogueado, int totalCajas, int totalMuestras) {

    public record FilaSondaje(String codigo, BigDecimal perforado, BigDecimal logueado, int cajas, int muestras) {
    }

    public record PuntoSerie(LocalDate fecha, BigDecimal metros) {
    }

    public static ReporteDiario de(LocalDate fecha, List<FilaSondaje> filas, List<PuntoSerie> serie) {
        throw new UnsupportedOperationException("pendiente");
    }
}
