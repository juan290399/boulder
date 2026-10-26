package com.boulder.boulder.fotos;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Almacén de fotos en el disco del servidor.
 *
 * <p>
 * Las claves son rutas relativas ({@code DDH-VERO-S-040-26/<id>.jpg}) y se
 * resuelven contra la carpeta configurada en {@code boulder.fotos.directorio}.
 * Se rechaza cualquier clave que, una vez resuelta, salga de esa carpeta: la
 * clave se arma con datos que llegan de fuera, y un {@code ../} bastaría para
 * escribir en cualquier parte del disco.
 * </p>
 *
 * <p>
 * La escritura va a un archivo temporal y después se mueve: si el proceso se
 * corta a mitad, no queda una foto a medias con el nombre bueno.
 * </p>
 */
@Component
public class AlmacenFotosDisco implements AlmacenFotos {

    private static final Logger log = LoggerFactory.getLogger(AlmacenFotosDisco.class);

    private final Path raiz;

    public AlmacenFotosDisco(@Value("${boulder.fotos.directorio:./datos/fotos}") String directorio) throws IOException {
        this.raiz = Path.of(directorio).toAbsolutePath().normalize();
        Files.createDirectories(raiz);
        log.info("Almacén de fotos en {}", raiz);
    }

    @Override
    public void guardar(String clave, byte[] contenido) throws IOException {
        Path destino = resolver(clave);
        Files.createDirectories(destino.getParent());
        Path temporal = Files.createTempFile(destino.getParent(), ".subida-", ".tmp");
        try {
            Files.write(temporal, contenido);
            Files.move(temporal, destino, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } finally {
            Files.deleteIfExists(temporal);
        }
    }

    @Override
    public InputStream abrir(String clave) throws IOException {
        return Files.newInputStream(resolver(clave));
    }

    @Override
    public boolean existe(String clave) {
        return Files.isRegularFile(resolver(clave));
    }

    @Override
    public void borrar(String clave) throws IOException {
        Files.deleteIfExists(resolver(clave));
    }

    private Path resolver(String clave) {
        Path ruta = raiz.resolve(clave).normalize();
        if (!ruta.startsWith(raiz)) {
            throw new IllegalArgumentException("Clave de almacén fuera de la carpeta de fotos: " + clave);
        }
        return ruta;
    }
}
