package com.boulder.boulder.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Desviación de una estación contra el plan. Los casos de azimut usan la
 * dirección de la campaña (≈ 299° y ≈ 119°) y el cruce por el norte.
 */
class DesviacionTest {

    @Test
    @DisplayName("Sin diferencia no hay desviación ni sentido")
    void sinDesviacion() {
        Desviacion d = Desviacion.calcular(100, 299.0, -49.4, 299.0, -49.4);
        assertThat(d.azimut()).isZero();
        assertThat(d.sentidoAzimut()).isNull();
        assertThat(d.inclinacion()).isZero();
        assertThat(d.sentidoInclinacion()).isNull();
        assertThat(d.total()).isCloseTo(0, within(1e-9));
        assertThat(d.metros()).isCloseTo(0, within(1e-9));
    }

    @ParameterizedTest(name = "medido {0}° contra plan {1}° → {2}° {3}")
    @CsvSource({
            "300.5, 299.0,  1.5, derecha",
            "297.0, 299.0, -2.0, izquierda",
            "2.0,   358.0,  4.0, derecha",
            "358.0, 2.0,   -4.0, izquierda",
    })
    @DisplayName("El azimut se compara por el camino corto, también al cruzar el norte")
    void azimut(double medido, double plan, double esperado, String sentido) {
        Desviacion d = Desviacion.calcular(50, medido, -60, plan, -60);
        assertThat(d.azimut()).isCloseTo(esperado, within(1e-9));
        assertThat(d.sentidoAzimut()).isEqualTo(sentido);
    }

    @Test
    @DisplayName("Inclinación medida mayor que la planificada: el pozo se levanta")
    void seLevanta() {
        Desviacion d = Desviacion.calcular(50, 299, -58.0, 299, -60.0);
        assertThat(d.inclinacion()).isCloseTo(2.0, within(1e-9));
        assertThat(d.sentidoInclinacion()).isEqualTo("arriba");
    }

    @Test
    @DisplayName("Inclinación medida menor que la planificada: el pozo se hunde")
    void seHunde() {
        Desviacion d = Desviacion.calcular(50, 299, -61.5, 299, -60.0);
        assertThat(d.inclinacion()).isCloseTo(-1.5, within(1e-9));
        assertThat(d.sentidoInclinacion()).isEqualTo("abajo");
    }

    @Test
    @DisplayName("En un pozo vertical el azimut no desvía nada")
    void vertical() {
        Desviacion d = Desviacion.calcular(100, 120, -90, 300, -90);
        assertThat(d.total()).isCloseTo(0, within(1e-6));
    }

    @Test
    @DisplayName("Un grado de desvío a 100 m separa unos 1.75 m")
    void metros() {
        Desviacion d = Desviacion.calcular(100, 299, -59.0, 299, -60.0);
        assertThat(d.total()).isCloseTo(1.0, within(1e-6));
        assertThat(d.metros()).isCloseTo(1.745, within(0.001));
    }

    @Test
    @DisplayName("El ángulo total combina azimut e inclinación")
    void combinado() {
        Desviacion d = Desviacion.calcular(200, 301, -58, 299, -60);
        assertThat(d.total()).isGreaterThan(2.0).isLessThan(2.0 + 1.1);
    }
}
