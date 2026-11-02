package com.boulder.boulder.reportes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.boulder.boulder.reportes.ReporteDiario.FilaSondaje;
import com.boulder.boulder.reportes.ReportePeriodo.FilaDesvio;
import com.boulder.boulder.reportes.ReportePeriodo.FilaDia;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReportePeriodoServiceTest {

    private static final LocalDate HOY = LocalDate.of(2026, 9, 30);
    private static final LocalDate DESDE = LocalDate.of(2026, 9, 1);
    private static final LocalDate HASTA = LocalDate.of(2026, 9, 7);

    @Mock
    private ReporteDiarioRepository repositorio;

    private ReportePeriodoService servicio;

    @BeforeEach
    void preparar() {
        ZoneId lima = ZoneId.of("America/Lima");
        servicio = new ReportePeriodoService(repositorio, Clock.fixed(HOY.atStartOfDay(lima).toInstant(), lima));
    }

    private static FilaSondaje sondaje(String codigo, String perf, String log, int cajas, int muestras) {
        return new FilaSondaje(codigo, new BigDecimal(perf), new BigDecimal(log), cajas, muestras);
    }

    private static FilaDia dia(LocalDate fecha, String perf) {
        return new FilaDia(fecha, new BigDecimal(perf), BigDecimal.ZERO, 0, 0);
    }

    @Test
    @DisplayName("Acumula por sondaje en el periodo, ordenado por código, con totales")
    void porSondaje() {
        when(repositorio.porSondaje(DESDE, HASTA)).thenReturn(List.of(
                sondaje("DDH-VERO-S-040-26", "120.50", "80.00", 30, 40),
                sondaje("DDH-VERO-S-038-26", "95.10", "95.10", 28, 35)));

        ReportePeriodo r = servicio.generar(DESDE, HASTA);

        assertThat(r.sondajes()).extracting(FilaSondaje::codigo)
                .containsExactly("DDH-VERO-S-038-26", "DDH-VERO-S-040-26");
        assertThat(r.totalPerforado()).isEqualByComparingTo("215.60");
        assertThat(r.totalLogueado()).isEqualByComparingTo("175.10");
        assertThat(r.totalCajas()).isEqualTo(58);
        assertThat(r.totalMuestras()).isEqualTo(75);
    }

    @Test
    @DisplayName("Una fila por día del periodo, también los días sin actividad")
    void porDia() {
        when(repositorio.porDia(DESDE, HASTA)).thenReturn(List.of(dia(HASTA, "20.20"), dia(DESDE, "15.00")));

        ReportePeriodo r = servicio.generar(DESDE, HASTA);

        assertThat(r.dias()).hasSize(7);
        assertThat(r.dias().getFirst().fecha()).isEqualTo(DESDE);
        assertThat(r.dias().getFirst().perforado()).isEqualByComparingTo("15.00");
        assertThat(r.dias().get(1).perforado()).isEqualByComparingTo("0");
        assertThat(r.dias().getLast().perforado()).isEqualByComparingTo("20.20");
    }

    @Test
    @DisplayName("Trae las mediciones de desvío del periodo, por fecha y profundidad")
    void desvios() {
        when(repositorio.desvios(DESDE, HASTA)).thenReturn(List.of(
                new FilaDesvio("DDH-VERO-S-040-26", new BigDecimal("150.0"), HASTA, 2.1, 5.5, "continuar"),
                new FilaDesvio("DDH-VERO-S-038-26", new BigDecimal("90.0"), DESDE, 0.8, 1.3, "continuar")));

        ReportePeriodo r = servicio.generar(DESDE, HASTA);

        assertThat(r.desvios()).extracting(FilaDesvio::fecha).containsExactly(DESDE, HASTA);
        assertThat(r.desviosFueraTolerancia()).isEqualTo(1);
    }

    @Test
    @DisplayName("Sin fechas: los siete días que terminan hoy")
    void porDefecto() {
        ReportePeriodo r = servicio.generar(null, null);

        assertThat(r.hasta()).isEqualTo(HOY);
        assertThat(r.desde()).isEqualTo(HOY.minusDays(6));
    }

    @Test
    @DisplayName("Rechaza un periodo invertido, futuro o de más de 92 días")
    void rangosInvalidos() {
        assertThatThrownBy(() -> servicio.generar(HASTA, DESDE)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> servicio.generar(DESDE, HOY.plusDays(1))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> servicio.generar(HOY.minusDays(92), HOY)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Un periodo de 92 días se acepta")
    void limite() {
        assertThat(servicio.generar(HOY.minusDays(91), HOY).dias()).hasSize(92);
    }
}
