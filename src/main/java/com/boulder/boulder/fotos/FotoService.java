package com.boulder.boulder.fotos;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boulder.boulder.entities.Foto;
import com.boulder.boulder.entities.Sondaje;
import com.boulder.boulder.eventos.CambioDatos;
import com.boulder.boulder.repositories.FotoRepository;
import com.boulder.boulder.repositories.SondajeRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

/**
 * Subida, consulta y baja de fotos de testigo.
 *
 * <p>
 * <b>Idempotente por id.</b> El cliente puede traer el id de la foto —el tablet
 * lo genera al tomarla—, así que reintentar una subida cortada no duplica
 * nada: si ese id ya existe con el mismo hash, se devuelve lo que había.
 * </p>
 *
 * <p>
 * <b>El tipo se decide por el contenido, no por lo que declara el cliente.</b>
 * Se aceptan JPEG y PNG reconociendo los primeros bytes del archivo; un
 * {@code .jpg} que en realidad es otra cosa se rechaza antes de tocar el disco.
 * </p>
 *
 * <p>
 * Junto a cada foto se guarda una miniatura de 480 px. La galería muestra
 * decenas de fotos de un sondaje a la vez, y bajar los originales de varios
 * megas cada uno la haría inservible con la conexión de un campamento.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class FotoService {

    private static final Logger log = LoggerFactory.getLogger(FotoService.class);
    private static final int ANCHO_MINIATURA = 480;

    private final FotoRepository fotos;
    private final SondajeRepository sondajes;
    private final AlmacenFotos almacen;
    private final ApplicationEventPublisher eventos;

    /** Los datos de la foto que no salen del propio archivo. */
    public record Metadatos(UUID sondajeId, Integer cajaDesde, Integer cajaHasta, BigDecimal desde,
            BigDecimal hasta, String condicion, OffsetDateTime tomadaEn, String observaciones) {
    }

    @Transactional
    public Foto subir(UUID id, Metadatos m, byte[] contenido, String nombreArchivo,
            String autor, String dispositivo) throws IOException {

        String tipo = tipoPorContenido(contenido);
        if (tipo == null) {
            throw new IllegalArgumentException("El archivo no es una imagen JPEG ni PNG.");
        }
        validar(m);
        Sondaje sondaje = sondajes.findById(m.sondajeId())
                .orElseThrow(() -> new IllegalArgumentException("El sondaje indicado no existe."));

        String hash = sha256(contenido);
        UUID idFoto = id != null ? id : UUID.randomUUID();

        Foto existente = fotos.findById(idFoto).orElse(null);
        if (existente != null && hash.equals(existente.getSha256()) && existente.getEliminadoEn() == null) {
            log.info("Foto {} ya estaba subida con el mismo contenido; no se repite", idFoto);
            return existente;
        }

        BufferedImage imagen = ImageIO.read(new ByteArrayInputStream(contenido));
        if (imagen == null) {
            throw new IllegalArgumentException("No se pudo leer la imagen: el archivo parece dañado.");
        }

        String extension = "image/png".equals(tipo) ? "png" : "jpg";
        String carpeta = sondaje.getCodigo().replaceAll("[^A-Za-z0-9._-]", "_");
        String clave = carpeta + "/" + idFoto + "." + extension;

        almacen.guardar(clave, contenido);
        almacen.guardar(claveMiniatura(clave), miniatura(imagen));

        Foto f = existente != null ? existente : new Foto();
        f.setId(idFoto);
        f.setSondajeId(m.sondajeId());
        f.setCajaDesde(m.cajaDesde());
        f.setCajaHasta(m.cajaHasta());
        f.setDesde(m.desde());
        f.setHasta(m.hasta());
        f.setCondicion(m.condicion());
        f.setTomadaEn(m.tomadaEn());
        f.setObservaciones(m.observaciones());
        f.setAutor(recortar(autor, 80));
        f.setDispositivo(recortar(dispositivo, 64));
        f.setArchivoOriginal(recortar(nombreArchivo, 255));
        f.setTipoMime(tipo);
        f.setBytes((long) contenido.length);
        f.setAncho(imagen.getWidth());
        f.setAlto(imagen.getHeight());
        f.setSha256(hash);
        f.setClaveAlmacen(clave);
        f.setEliminadoEn(null);
        Foto guardada = fotos.save(f);

        eventos.publishEvent(CambioDatos.de("foto", Set.of(m.sondajeId()),
                dispositivo != null ? "tablet" : "web", 1));
        log.info("Foto {} de {} guardada ({} bytes, {}x{})", idFoto, sondaje.getCodigo(),
                contenido.length, imagen.getWidth(), imagen.getHeight());
        return guardada;
    }

    @Transactional(readOnly = true)
    public List<Foto> listar(UUID sondajeId) {
        return fotos.vigentesDe(sondajeId);
    }

    @Transactional(readOnly = true)
    public Foto obtener(UUID id) {
        Foto f = fotos.findById(id).orElseThrow(() -> new EntityNotFoundException("Foto no encontrada: " + id));
        if (f.getEliminadoEn() != null) {
            throw new EntityNotFoundException("Foto eliminada: " + id);
        }
        return f;
    }

    public InputStream contenido(Foto f, boolean miniatura) throws IOException {
        String clave = miniatura ? claveMiniatura(f.getClaveAlmacen()) : f.getClaveAlmacen();
        if (miniatura && !almacen.existe(clave)) {
            clave = f.getClaveAlmacen();
        }
        return almacen.abrir(clave);
    }

    /**
     * Baja lógica. El archivo se conserva: una foto de testigo borrada por
     * error no se puede volver a tomar si el testigo ya se cortó.
     */
    @Transactional
    public void eliminar(UUID id) {
        Foto f = obtener(id);
        f.setEliminadoEn(OffsetDateTime.now());
        fotos.save(f);
        eventos.publishEvent(CambioDatos.de("foto", Set.of(f.getSondajeId()), "web", 1));
    }

    // ------------------------------------------------------------------

    private void validar(Metadatos m) {
        if (m.sondajeId() == null) {
            throw new IllegalArgumentException("Falta el sondaje de la foto.");
        }
        if (m.condicion() != null && !m.condicion().isBlank()
                && !m.condicion().equals("seco") && !m.condicion().equals("humedo")) {
            throw new IllegalArgumentException("La condición tiene que ser «seco» o «humedo».");
        }
        if (m.desde() != null && m.hasta() != null && m.hasta().compareTo(m.desde()) < 0) {
            throw new IllegalArgumentException("El tramo está invertido: «hasta» es menor que «desde».");
        }
        if (m.cajaDesde() != null && m.cajaHasta() != null && m.cajaHasta() < m.cajaDesde()) {
            throw new IllegalArgumentException("Las cajas están invertidas.");
        }
    }

    /** JPEG empieza con FF D8 FF; PNG con 89 50 4E 47. */
    static String tipoPorContenido(byte[] b) {
        if (b.length > 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (b.length > 4 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') {
            return "image/png";
        }
        return null;
    }

    private static String claveMiniatura(String clave) {
        int punto = clave.lastIndexOf('.');
        return (punto > 0 ? clave.substring(0, punto) : clave) + "_min.jpg";
    }

    private static byte[] miniatura(BufferedImage original) throws IOException {
        int ancho = Math.min(ANCHO_MINIATURA, original.getWidth());
        int alto = Math.max(1, Math.round(original.getHeight() * (ancho / (float) original.getWidth())));
        BufferedImage chica = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = chica.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(original, 0, 0, ancho, alto, null);
        } finally {
            g.dispose();
        }
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        ImageIO.write(chica, "jpg", salida);
        return salida.toByteArray();
    }

    private static String sha256(byte[] datos) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(datos));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    static String nombreArchivoSeguro(String recibido) {
        throw new UnsupportedOperationException("pendiente");
    }

    private static String recortar(String s, int max) {
        return s == null ? null : (s.length() > max ? s.substring(0, max) : s);
    }
}
