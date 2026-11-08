package com.boulder.boulder.monitoreo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;

import com.boulder.boulder.fotos.AlmacenFotosDisco;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class TareasMantenimientoTest {

    private static final Instant AHORA = Instant.parse("2026-11-07T08:30:00Z");

    @TempDir
    Path carpeta;

    private JdbcTemplate jdbc;
    private SimpleMeterRegistry registro;
    private TareasMantenimiento tareas;

    @BeforeEach
    void preparar() throws Exception {
        jdbc = mock(JdbcTemplate.class);
        registro = new SimpleMeterRegistry();
        tareas = new TareasMantenimiento(new AlmacenFotosDisco(carpeta.toString()), jdbc,
                Clock.fixed(AHORA, ZoneOffset.UTC), registro);
    }

    private Path archivo(String ruta, Duration edad) throws Exception {
        Path p = carpeta.resolve(ruta);
        Files.createDirectories(p.getParent());
        Files.write(p, new byte[] {1, 2, 3});
        Files.setLastModifiedTime(p, FileTime.from(AHORA.minus(edad)));
        return p;
    }

    @Test
    @DisplayName("Borra las subidas a medias de más de un día y deja las recientes y las fotos")
    void purgaTemporalesViejos() throws Exception {
        Path viejo = archivo("2026/11/.subida-123.tmp", Duration.ofHours(30));
        Path reciente = archivo("2026/11/.subida-456.tmp", Duration.ofMinutes(10));
        Path foto = archivo("2026/11/abc.jpg", Duration.ofDays(40));

        int borrados = tareas.purgarTemporales();

        assertThat(borrados).isEqualTo(1);
        assertThat(viejo).doesNotExist();
        assertThat(reciente).exists();
        assertThat(foto).exists();
    }

    @Test
    @DisplayName("Los dispositivos de GeoLog sin contacto quedan en una métrica")
    void dispositivosSinContacto() {
        when(jdbc.queryForObject(anyString(), eq(Integer.class))).thenReturn(2);

        assertThat(tareas.revisarDispositivos()).isEqualTo(2);
        assertThat(registro.get(TareasMantenimiento.SIN_CONTACTO).gauge().value()).isEqualTo(2.0);
    }

    @Test
    @DisplayName("Si la base no responde, la tarea no revienta y deja la métrica como estaba")
    void baseCaida() {
        when(jdbc.queryForObject(anyString(), eq(Integer.class)))
                .thenThrow(new DataAccessResourceFailureException("sin conexión"));

        assertThat(tareas.revisarDispositivos()).isEqualTo(-1);
        assertThat(registro.get(TareasMantenimiento.SIN_CONTACTO).gauge().value()).isZero();
    }
}
