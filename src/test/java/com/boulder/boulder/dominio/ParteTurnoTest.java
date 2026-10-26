package com.boulder.boulder.dominio;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * El parte de turno que hoy llega por WhatsApp: «de 120.50 a 135.20, avance
 * 14.70». Los casos de error son los que aparecen en los partes reales.
 */
class ParteTurnoTest {

    private static BigDecimal m(String metros) {
        return metros == null ? null : new BigDecimal(metros);
    }

    private static List<String> revisar(String desde, String hasta, String avance) {
        return ParteTurno.errores(m(desde), m(hasta), m(avance), null, null, null);
    }

    @Test
    @DisplayName("Un parte que cuadra no tiene errores")
    void cuadra() {
        assertThat(revisar("120.50", "135.20", "14.70")).isEmpty();
    }

    @Test
    @DisplayName("La escala no importa: 14.7 es lo mismo que 14.70")
    void escala() {
        assertThat(revisar("120.5", "135.20", "14.7")).isEmpty();
    }

    @Test
    @DisplayName("Un turno sin avance (máquina en mantenimiento) es válido")
    void sinAvance() {
        assertThat(revisar("135.20", "135.20", "0")).isEmpty();
    }

    @Test
    @DisplayName("Hasta menor que desde se rechaza")
    void hastaMenor() {
        assertThat(revisar("135.20", "120.50", "-14.70"))
                .anyMatch(e -> e.contains("no puede ser menor que desde"));
    }

    @Test
    @DisplayName("Si el avance no es hasta menos desde, se rechaza con la resta correcta")
    void avanceNoCuadra() {
        assertThat(revisar("120.50", "135.20", "17.40"))
                .singleElement().asString()
                .contains("17.40").contains("14.70");
    }

    @Test
    @DisplayName("Una profundidad negativa se rechaza")
    void negativa() {
        assertThat(revisar("-1.00", "5.00", "6.00")).anyMatch(e -> e.contains("desde"));
    }

    @ParameterizedTest(name = "retorno {0} %")
    @ValueSource(strings = { "-5", "100.5", "250" })
    @DisplayName("El retorno de agua es un porcentaje entre 0 y 100")
    void retornoFueraDeRango(String retorno) {
        assertThat(ParteTurno.errores(m("0"), m("10"), m("10"), m(retorno), null, null))
                .anyMatch(e -> e.contains("retorno"));
    }

    @Test
    @DisplayName("El casing no puede terminar antes de empezar")
    void casingInvertido() {
        assertThat(ParteTurno.errores(m("0"), m("30"), m("30"), null, m("12"), m("6")))
                .anyMatch(e -> e.contains("casing"));
    }

    @Test
    @DisplayName("El casing no puede bajar más que el pozo")
    void casingBajoElFondo() {
        assertThat(ParteTurno.errores(m("0"), m("30"), m("30"), null, m("0"), m("45")))
                .anyMatch(e -> e.contains("casing"));
    }

    @Test
    @DisplayName("Un casing dentro del pozo es válido")
    void casingValido() {
        assertThat(ParteTurno.errores(m("0"), m("30"), m("30"), m("85"), m("0"), m("12"))).isEmpty();
    }
}
