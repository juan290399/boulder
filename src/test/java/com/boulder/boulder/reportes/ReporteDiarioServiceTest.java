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

/**
 * Reporte diario consolidado (RF15, CU22): lo que hoy se arma a mano copiando de
 * cinco reportes. El repositorio es un mock; el reloj está fijo.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReporteDiarioServiceTest {

    private static final LocalDate DIA = LocalDate.of(2026, 9, 7);

    @Mock
    private ReporteDiarioRepository repositorio;

    private ReporteDiarioService servicio;

    @BeforeEach
    void preparar() {
        Clock reloj = Clock.fixed(DIA.plusDays(1).atStartOfDay(ZoneId.of("America/Lima")).toInstant(),
                ZoneId.of("America/Lima"));
        servicio = new ReporteDiarioService(repositorio, reloj);
    }

    private static FilaSondaje fila(String codigo, String perf, String log, int cajas, int muestras) {
        return new FilaSondaje(codigo, new BigDecimal(perf), new BigDecimal(log), cajas, muestras);
    }

    @Test
    @DisplayName("Los totales suman las filas por sondaje")
    void totales() {
        when(repositorio.porSondaje(DIA)).thenReturn(List.of(
                fila("DDH-VERO-S-040-26", "20.20", "35.00", 6, 12),
                fila("DDH-VERO-S-038-26", "28.30", "0", 9, 0)));

        ReporteDiario r = servicio.generar(DIA);

        assertThat(r.fecha()).isEqualTo(DIA);
        assertThat(r.totalPerforado()).isEqualByComparingTo("48.50");
        assertThat(r.totalLogueado()).isEqualByComparingTo("35.00");
        assertThat(r.totalCajas()).isEqualTo(15);
        assertThat(r.totalMuestras()).isEqualTo(12);
    }

    @Test
    @DisplayName("Los sondajes salen ordenados por código")
    void orden() {
        when(repositorio.porSondaje(DIA)).thenReturn(List.of(
                fila("DDH-VERO-S-040-26", "1", "0", 0, 0),
                fila("DDH-VERO-S-038-26", "1", "0", 0, 0)));

        assertThat(servicio.generar(DIA).sondajes())
                .extracting(FilaSondaje::codigo)
                .containsExactly("DDH-VERO-S-038-26", "DDH-VERO-S-040-26");
    }

    @Test
    @DisplayName("Un día sin partes da un reporte en cero, no un error")
    void diaVacio() {
        when(repositorio.porSondaje(DIA)).thenReturn(List.of());

        ReporteDiario r = servicio.generar(DIA);

        assertThat(r.sondajes()).isEmpty();
        assertThat(r.totalPerforado()).isEqualByComparingTo("0");
        assertThat(r.totalCajas()).isZero();
    }

    @Test
    @DisplayName("Sin fecha se toma el día de hoy en Lima")
    void hoy() {
        when(repositorio.porSondaje(DIA.plusDays(1))).thenReturn(List.of());
        assertThat(servicio.generar(null).fecha()).isEqualTo(DIA.plusDays(1));
    }

    @Test
    @DisplayName("No hay reporte de un día que todavía no pasó")
    void futuro() {
        assertThatThrownBy(() -> servicio.generar(DIA.plusDays(5)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("El resumen de campaña llega hasta la fecha del reporte, con su total")
    void campania() {
        when(repositorio.porSondaje(DIA)).thenReturn(List.of());
        when(repositorio.campania(DIA)).thenReturn(List.of(
                new ReporteDiario.FilaCampania("DDH-VERO-S-040-26", "C8C", DIA.minusDays(3), DIA, 3,
                        new BigDecimal("120.50"), "En proceso"),
                new ReporteDiario.FilaCampania("DDH-VERO-S-038-26", "H-400", DIA.minusDays(20), DIA.minusDays(5), 15,
                        new BigDecimal("300.00"), "Finalizado")));

        ReporteDiario r = servicio.generar(DIA);

        assertThat(r.campania()).extracting(ReporteDiario.FilaCampania::codigo)
                .containsExactly("DDH-VERO-S-038-26", "DDH-VERO-S-040-26");
        assertThat(r.totalCampania()).isEqualByComparingTo("420.50");
    }

    @Test
    @DisplayName("El avance del día por máquina, ordenado por máquina")
    void porMaquina() {
        when(repositorio.porSondaje(DIA)).thenReturn(List.of());
        when(repositorio.porMaquina(DIA)).thenReturn(List.of(
                new ReporteDiario.FilaMaquina("H-600", new BigDecimal("18.70")),
                new ReporteDiario.FilaMaquina("H-400", new BigDecimal("19.40"))));

        assertThat(servicio.generar(DIA).porMaquina()).extracting(ReporteDiario.FilaMaquina::maquina)
                .containsExactly("H-400", "H-600");
    }

    @Test
    @DisplayName("La matriz mensual tiene una columna por mes de la campaña y ceros donde no hubo avance")
    void mensual() {
        when(repositorio.porSondaje(DIA)).thenReturn(List.of());
        when(repositorio.mensual(DIA)).thenReturn(List.of(
                new ReporteDiario.CeldaMensual("DDH-VERO-S-038-26", java.time.YearMonth.of(2026, 8), new BigDecimal("100")),
                new ReporteDiario.CeldaMensual("DDH-VERO-S-038-26", java.time.YearMonth.of(2026, 9), new BigDecimal("50")),
                new ReporteDiario.CeldaMensual("DDH-VERO-S-040-26", java.time.YearMonth.of(2026, 9), new BigDecimal("20"))));

        ReporteDiario.Mensual m = servicio.generar(DIA).mensual();

        assertThat(m.meses()).containsExactly(java.time.YearMonth.of(2026, 8), java.time.YearMonth.of(2026, 9));
        assertThat(m.filas()).hasSize(2);
        assertThat(m.filas().get(1).metros()).containsExactly(BigDecimal.ZERO, new BigDecimal("20"));
        assertThat(m.totales()).containsExactly(new BigDecimal("100"), new BigDecimal("70"));
    }

    @Test
    @DisplayName("La serie trae los siete días que terminan en la fecha, también los que no tienen avance")
    void serie() {
        when(repositorio.porSondaje(DIA)).thenReturn(List.of());
        when(repositorio.perforadoPorDia(DIA.minusDays(6), DIA)).thenReturn(java.util.Map.of(
                DIA, new BigDecimal("20.20"), DIA.minusDays(1), new BigDecimal("28.30")));

        ReporteDiario r = servicio.generar(DIA);

        assertThat(r.ultimosDias()).hasSize(7);
        assertThat(r.ultimosDias().getFirst().fecha()).isEqualTo(DIA.minusDays(6));
        assertThat(r.ultimosDias().getFirst().metros()).isEqualByComparingTo("0");
        assertThat(r.ultimosDias().getLast().metros()).isEqualByComparingTo("20.20");
    }
}
