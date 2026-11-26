package com.boulder.boulder.monitoreo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import com.boulder.boulder.fotos.AlmacenFotosDisco;

/**
 * Componente {@code almacenFotos} de {@code /actuator/health}.
 *
 * <p>
 * El {@code diskSpace} de Spring mira el disco donde corre el proceso; las
 * fotos pueden estar en otro volumen. Si esa carpeta se llena o deja de ser
 * escribible, las subidas de GeoLog fallan aunque el resto del sistema siga
 * bien, así que tiene su propio indicador.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Component("almacenFotos")
public class SaludAlmacenFotos implements HealthIndicator {

    private static final long MB = 1024L * 1024L;

    private final AlmacenFotosDisco almacen;
    private final long minimoMb;

    public SaludAlmacenFotos(AlmacenFotosDisco almacen,
                             @Value("${boulder.fotos.minimo-libre-mb:1024}") long minimoMb) {
        this.almacen = almacen;
        this.minimoMb = minimoMb;
    }

    @Override
    public Health health() {
        Path raiz = almacen.raiz();
        if (!Files.isDirectory(raiz) || !Files.isWritable(raiz)) {
            return Health.down().withDetail("motivo", "la carpeta de fotos no existe o no se puede escribir").build();
        }
        try {
            long libreMb = Files.getFileStore(raiz).getUsableSpace() / MB;
            Health.Builder estado = libreMb >= minimoMb ? Health.up() : Health.down();
            return estado.withDetail("libreMb", libreMb).withDetail("minimoMb", minimoMb).build();
        } catch (IOException e) {
            return Health.down().withDetail("motivo", "no se pudo leer el espacio libre").build();
        }
    }
}
