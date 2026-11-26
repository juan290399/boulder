package com.boulder.boulder.monitoreo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.boulder.boulder.fotos.AlmacenFotosDisco;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * Tareas de mantenimiento que corre la propia aplicación con expresiones cron.
 *
 * <p>
 * Complementan a las del sistema operativo ({@code scripts/mantenimiento}):
 * aquellas respaldan la base y vigilan el servicio desde fuera; estas limpian y
 * revisan lo que solo la aplicación conoce. Los horarios se cambian sin
 * recompilar con {@code boulder.mantenimiento.cron.*}.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Component
public class TareasMantenimiento {

    /** Dispositivos de GeoLog activos en la última semana pero sin contacto hace más de un día. */
    public static final String SIN_CONTACTO = "boulder.geolog.dispositivos.sin_contacto";

    private static final Logger log = LoggerFactory.getLogger(TareasMantenimiento.class);
    private static final Duration EDAD_TEMPORAL = Duration.ofHours(24);

    private final Path fotos;
    private final JdbcTemplate jdbc;
    private final Clock reloj;
    private final AtomicInteger sinContacto = new AtomicInteger();

    public TareasMantenimiento(AlmacenFotosDisco almacen, JdbcTemplate jdbc, Clock reloj, MeterRegistry registro) {
        this.fotos = almacen.raiz();
        this.jdbc = jdbc;
        this.reloj = reloj;
        Gauge.builder(SIN_CONTACTO, sinContacto, AtomicInteger::get)
                .description("Tablets de GeoLog que dejaron de sincronizar (más de 24 h sin contacto)")
                .register(registro);
    }

    /**
     * Borra las subidas de fotos que quedaron a medias: el almacén escribe en
     * un {@code .subida-*.tmp} y lo renombra al terminar, así que uno que
     * sobrevive más de un día es de un proceso que se cortó.
     *
     * @return cantidad de archivos borrados
     */
    @Scheduled(cron = "${boulder.mantenimiento.cron.temporales:0 30 3 * * *}", zone = "America/Lima")
    public int purgarTemporales() {
        Instant limite = reloj.instant().minus(EDAD_TEMPORAL);
        List<Path> viejos;
        try (Stream<Path> archivos = Files.walk(fotos)) {
            viejos = archivos.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().startsWith(".subida-"))
                    .filter(p -> p.getFileName().toString().endsWith(".tmp"))
                    .filter(p -> modificadoAntesDe(p, limite))
                    .toList();
        } catch (IOException e) {
            log.warn("No se pudo recorrer el almacén de fotos para purgar temporales", e);
            return 0;
        }
        int borrados = 0;
        for (Path p : viejos) {
            try {
                Files.deleteIfExists(p);
                borrados++;
            } catch (IOException e) {
                log.warn("No se pudo borrar el temporal {}", p.getFileName(), e);
            }
        }
        log.info("Mantenimiento: {} subidas de fotos a medias purgadas", borrados);
        return borrados;
    }

    /**
     * Cuenta las tablets que sincronizaban en la última semana y llevan más de
     * un día calladas, y deja el valor en la métrica {@link #SIN_CONTACTO}.
     *
     * @return dispositivos sin contacto, o -1 si la base no respondió
     */
    @Scheduled(cron = "${boulder.mantenimiento.cron.dispositivos:0 */15 * * * *}", zone = "America/Lima")
    public int revisarDispositivos() {
        try {
            Integer n = jdbc.queryForObject("""
                    SELECT count(*) FROM logueo.dispositivo_sync
                    WHERE ultimo_contacto < now() - interval '24 hours'
                      AND ultimo_contacto > now() - interval '7 days'
                    """, Integer.class);
            int valor = n != null ? n : 0;
            sinContacto.set(valor);
            if (valor > 0) {
                log.warn("Mantenimiento: {} tablets de GeoLog sin sincronizar hace más de 24 h", valor);
            }
            return valor;
        } catch (DataAccessException e) {
            log.warn("Mantenimiento: no se pudo revisar la sincronización de los dispositivos: {}", e.getMessage());
            return -1;
        }
    }

    private static boolean modificadoAntesDe(Path p, Instant limite) {
        try {
            return Files.getLastModifiedTime(p).toInstant().isBefore(limite);
        } catch (IOException e) {
            return false;
        }
    }
}
