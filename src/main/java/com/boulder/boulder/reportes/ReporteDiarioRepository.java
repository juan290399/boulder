package com.boulder.boulder.reportes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.boulder.boulder.reportes.ReporteDiario.FilaSondaje;

@Repository
public class ReporteDiarioRepository {

    public List<FilaSondaje> porSondaje(LocalDate fecha) {
        throw new UnsupportedOperationException("pendiente");
    }

    public Map<LocalDate, BigDecimal> perforadoPorDia(LocalDate desde, LocalDate hasta) {
        throw new UnsupportedOperationException("pendiente");
    }
}
