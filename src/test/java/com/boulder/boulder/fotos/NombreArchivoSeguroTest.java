package com.boulder.boulder.fotos;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

/**
 * El nombre original de la foto lo manda el cliente, se guarda y después se
 * muestra en la galería: solo debe quedar el nombre del archivo, sin rutas ni
 * caracteres de control.
 */
class NombreArchivoSeguroTest {

    @ParameterizedTest(name = "«{0}» → «{1}»")
    @CsvSource(delimiter = '|', value = {
            "DDH-VERO-S-040-26_C133-142_247.25-283.95_S.jpg | DDH-VERO-S-040-26_C133-142_247.25-283.95_S.jpg",
            "C:\\fakepath\\caja_12.jpg                     | caja_12.jpg",
            "../../etc/passwd                              | passwd",
            "/home/geologo/fotos/caja 3.png                | caja 3.png",
    })
    @DisplayName("Solo queda el nombre del archivo, sin la ruta")
    void quitaRuta(String recibido, String esperado) {
        assertThat(FotoService.nombreArchivoSeguro(recibido)).isEqualTo(esperado);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Sin nombre no se inventa uno")
    void sinNombre(String recibido) {
        assertThat(FotoService.nombreArchivoSeguro(recibido)).isNull();
    }

    @org.junit.jupiter.api.Test
    @DisplayName("Los caracteres de control se eliminan y el largo se limita a 255")
    void controlYLargo() {
        assertThat(FotoService.nombreArchivoSeguro("caja\r\n12\u0000.jpg")).isEqualTo("caja12.jpg");
        assertThat(FotoService.nombreArchivoSeguro("a".repeat(400) + ".jpg")).hasSizeLessThanOrEqualTo(255);
    }
}
