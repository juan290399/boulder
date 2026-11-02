package com.boulder.boulder.reportes;

import java.math.BigDecimal;
import java.util.Locale;

import com.boulder.boulder.dto.EtapaAvanceDTO;
import com.boulder.boulder.dto.FrenteAvanceDTO;

/**
 * Estado de una etapa del flujo de testigo, como lo escribe el reporte diario de
 * campo: en blanco si no empezó, la profundidad alcanzada y «Proceso» si va
 * atrasada, y «Finalizado» (o «Al día» si el sondaje sigue perforándose) si
 * alcanzó lo perforado.
 */
public final class EstadoEtapa {

    private EstadoEtapa() {
    }

    /**
     * @param etapa   la etapa con su profundidad y su rezago
     * @param sondaje el sondaje al que pertenece
     * @return el estado para mostrar
     */
    public static String de(EtapaAvanceDTO etapa, FrenteAvanceDTO sondaje) {
        BigDecimal alcanzado = etapa.getProfundidad();
        if (alcanzado == null || alcanzado.signum() <= 0) {
            return "";
        }
        if (etapa.getRezago() != null && etapa.getRezago().signum() == 0) {
            return sondaje.getFechaFin() != null ? "Finalizado" : "Al día";
        }
        return String.format(Locale.ROOT, "%.2f", alcanzado).replace('.', ',') + " Proceso";
    }
}
