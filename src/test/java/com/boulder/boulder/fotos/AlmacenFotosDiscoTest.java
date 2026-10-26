package com.boulder.boulder.fotos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * El almacén en disco recibe claves armadas con datos que llegan de fuera: nunca
 * debe escribir ni leer fuera de su carpeta.
 */
class AlmacenFotosDiscoTest {

    @TempDir
    Path carpeta;

    private AlmacenFotosDisco almacen;

    @BeforeEach
    void preparar() throws IOException {
        almacen = new AlmacenFotosDisco(carpeta.resolve("fotos").toString());
    }

    @Test
    @DisplayName("Lo que se guarda se lee igual, byte a byte")
    void idaYVuelta() throws IOException {
        byte[] contenido = "contenido de prueba".getBytes(StandardCharsets.UTF_8);
        almacen.guardar("DDH-VERO-S-040-26/abc.jpg", contenido);

        assertThat(almacen.existe("DDH-VERO-S-040-26/abc.jpg")).isTrue();
        try (InputStream in = almacen.abrir("DDH-VERO-S-040-26/abc.jpg")) {
            assertThat(IOUtils.toByteArray(in)).isEqualTo(contenido);
        }
    }

    @Test
    @DisplayName("Guardar no deja archivos temporales")
    void sinTemporales() throws IOException {
        almacen.guardar("S1/a.jpg", new byte[] {1, 2, 3});
        try (var archivos = Files.list(carpeta.resolve("fotos/S1"))) {
            assertThat(archivos).extracting(p -> p.getFileName().toString()).containsExactly("a.jpg");
        }
    }

    @ParameterizedTest(name = "«{0}» se rechaza")
    @ValueSource(strings = {"../fuera.jpg", "S1/../../fuera.jpg", "../../Windows/system.ini"})
    @DisplayName("Una clave que sale de la carpeta se rechaza")
    void rechazaSalirDeLaCarpeta(String clave) {
        assertThatThrownBy(() -> almacen.guardar(clave, new byte[] {1}))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(carpeta.resolve("fuera.jpg")).doesNotExist();
    }

    @Test
    @DisplayName("Borrar una foto que no existe no falla")
    void borrarInexistente() throws IOException {
        almacen.borrar("S1/no-existe.jpg");
        assertThat(almacen.existe("S1/no-existe.jpg")).isFalse();
    }
}
