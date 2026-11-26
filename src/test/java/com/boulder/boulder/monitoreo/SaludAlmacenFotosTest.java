package com.boulder.boulder.monitoreo;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;

import com.boulder.boulder.fotos.AlmacenFotosDisco;

class SaludAlmacenFotosTest {

    @TempDir
    Path carpeta;

    @Test
    @DisplayName("Carpeta escribible y con espacio: UP con el espacio libre en el detalle")
    void up() throws Exception {
        Health h = new SaludAlmacenFotos(new AlmacenFotosDisco(carpeta.toString()), 1).health();

        assertThat(h.getStatus()).isEqualTo(Status.UP);
        assertThat(h.getDetails()).containsKeys("libreMb", "minimoMb");
    }

    @Test
    @DisplayName("Menos espacio libre que el mínimo: DOWN")
    void sinEspacio() throws Exception {
        Health h = new SaludAlmacenFotos(new AlmacenFotosDisco(carpeta.toString()), Long.MAX_VALUE / 2).health();

        assertThat(h.getStatus()).isEqualTo(Status.DOWN);
    }

    @Test
    @DisplayName("La carpeta desapareció: DOWN con el motivo")
    void sinCarpeta() throws Exception {
        Path sub = carpeta.resolve("fotos");
        AlmacenFotosDisco almacen = new AlmacenFotosDisco(sub.toString());
        java.nio.file.Files.delete(sub);

        Health h = new SaludAlmacenFotos(almacen, 1).health();

        assertThat(h.getStatus()).isEqualTo(Status.DOWN);
        assertThat(h.getDetails()).containsKey("motivo");
    }
}
