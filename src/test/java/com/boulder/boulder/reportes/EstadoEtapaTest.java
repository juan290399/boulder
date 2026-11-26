package com.boulder.boulder.reportes;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.boulder.boulder.dto.EtapaAvanceDTO;
import com.boulder.boulder.dto.FrenteAvanceDTO;

class EstadoEtapaTest {

    private static FrenteAvanceDTO sondaje(LocalDate fin) {
        FrenteAvanceDTO f = new FrenteAvanceDTO();
        f.setSondajeCodigo("DDH-VERO-S-038-26");
        f.setFechaFin(fin);
        return f;
    }

    private static EtapaAvanceDTO etapa(String profundidad, String rezago) {
        return new EtapaAvanceDTO("logueo", "Logueo geológico",
                profundidad == null ? null : new BigDecimal(profundidad),
                rezago == null ? null : new BigDecimal(rezago), null);
    }

    @Test
    @DisplayName("Sin avance la etapa queda en blanco")
    void sinIniciar() {
        assertThat(EstadoEtapa.de(etapa(null, "300"), sondaje(null))).isEmpty();
        assertThat(EstadoEtapa.de(etapa("0", "300"), sondaje(null))).isEmpty();
    }

    @Test
    @DisplayName("Con rezago, la profundidad alcanzada y «Proceso», con coma decimal")
    void enProceso() {
        assertThat(EstadoEtapa.de(etapa("79.1", "220.9"), sondaje(null))).isEqualTo("79,10 Proceso");
    }

    @Test
    @DisplayName("Sin rezago en un sondaje terminado, «Finalizado»; si sigue perforándose, «Al día»")
    void alcanzada() {
        assertThat(EstadoEtapa.de(etapa("300", "0"), sondaje(LocalDate.of(2026, 9, 1)))).isEqualTo("Finalizado");
        assertThat(EstadoEtapa.de(etapa("300", "0"), sondaje(null))).isEqualTo("Al día");
    }
}
