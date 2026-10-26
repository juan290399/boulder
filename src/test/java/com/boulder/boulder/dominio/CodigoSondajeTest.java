package com.boulder.boulder.dominio;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * El mismo sondaje llega escrito de muchas formas en los reportes de campo
 * (WhatsApp, Excel de almacén, libros de logueo). Estos casos salen de los
 * reportes reales de la campaña VERONICA.
 */
class CodigoSondajeTest {

    @ParameterizedTest(name = "«{0}» → {1}")
    @CsvSource(delimiter = '|', value = {
            "DDH-VERO-S-038-26     | DDH-VERO-S-038-26",
            "Sondaje-vero-s-38-26  | DDH-VERO-S-038-26",
            "VERO-S-40-26          | DDH-VERO-S-040-26",
            "VERO S 38_26          | DDH-VERO-S-038-26",
            "DDH-VERO-S039-26      | DDH-VERO-S-039-26",
            "'  ddh vero s 7 26 '  | DDH-VERO-S-007-26",
            "Sondáje VERO S 38 26  | DDH-VERO-S-038-26",
    })
    @DisplayName("Las variantes de los reportes se reconocen como el código canónico")
    void normalizaVariantes(String escrito, String canonico) {
        assertThat(CodigoSondaje.normalizar(escrito)).contains(canonico);
    }

    @ParameterizedTest(name = "«{0}» no se adivina")
    @ValueSource(strings = {"DDH-35", "Sondaje 036", "DDH-VERO-S-1234-26", "VERO-S-38"})
    @DisplayName("Sin proyecto, sector, número y año no se adivina el sondaje")
    void rechazaCodigosIncompletos(String escrito) {
        assertThat(CodigoSondaje.normalizar(escrito)).isEmpty();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rechazaVacios(String escrito) {
        assertThat(CodigoSondaje.normalizar(escrito)).isEmpty();
    }

    @Test
    @DisplayName("Dos escrituras del mismo sondaje son equivalentes")
    void equivalencia() {
        assertThat(CodigoSondaje.mismoSondaje("VERO S 38_26", "Sondaje-vero-s-38-26")).isTrue();
        assertThat(CodigoSondaje.mismoSondaje("VERO S 38_26", "VERO S 39_26")).isFalse();
        assertThat(CodigoSondaje.mismoSondaje("DDH-35", "DDH-35")).isFalse();
    }
}
