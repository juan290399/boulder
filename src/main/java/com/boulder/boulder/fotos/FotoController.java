package com.boulder.boulder.fotos;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.boulder.boulder.entities.Foto;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

/**
 * Fotos de cajas de testigo.
 *
 * <p>
 * La subida es un {@code PUT} con el id en la ruta, igual que el logueo: el
 * cliente pone el id, y repetir la petición deja el mismo resultado. Sirve
 * tanto para el tablet como para la carga desde el navegador.
 * </p>
 *
 * <p>
 * Las imágenes se sirven por este mismo controlador y con token, no como
 * archivos estáticos: una foto de testigo es información de la empresa.
 * Como el id es un UUID y el contenido no cambia, se deja cachear una semana.
 * </p>
 */
@RestController
@RequestMapping("/api/fotos")
@RequiredArgsConstructor
public class FotoController {

    private final FotoService servicio;

    @GetMapping
    public List<Foto> listar(@RequestParam UUID sondajeId) {
        return servicio.listar(sondajeId);
    }

    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Foto subir(@PathVariable UUID id,
            @RequestPart("archivo") MultipartFile archivo,
            @RequestParam UUID sondajeId,
            @RequestParam(required = false) Integer cajaDesde,
            @RequestParam(required = false) Integer cajaHasta,
            @RequestParam(required = false) BigDecimal desde,
            @RequestParam(required = false) BigDecimal hasta,
            @RequestParam(required = false) String condicion,
            @RequestParam(required = false) OffsetDateTime tomadaEn,
            @RequestParam(required = false) String observaciones,
            @RequestHeader(value = "X-Dispositivo", required = false) String dispositivo,
            Principal principal) throws IOException {

        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("No llegó ningún archivo.");
        }
        FotoService.Metadatos m = new FotoService.Metadatos(sondajeId, cajaDesde, cajaHasta, desde, hasta,
                condicion == null || condicion.isBlank() ? null : condicion, tomadaEn, observaciones);
        return servicio.subir(id, m, archivo.getBytes(), archivo.getOriginalFilename(),
                principal != null ? principal.getName() : null, dispositivo);
    }

    @GetMapping("/{id}/contenido")
    public ResponseEntity<InputStreamResource> contenido(@PathVariable UUID id) throws IOException {
        return imagen(id, false);
    }

    @GetMapping("/{id}/miniatura")
    public ResponseEntity<InputStreamResource> miniatura(@PathVariable UUID id) throws IOException {
        return imagen(id, true);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<InputStreamResource> imagen(UUID id, boolean miniatura) throws IOException {
        Foto f = servicio.obtener(id);
        InputStream in = servicio.contenido(f, miniatura);
        MediaType tipo = miniatura ? MediaType.IMAGE_JPEG : MediaType.parseMediaType(f.getTipoMime());
        return ResponseEntity.ok()
                .contentType(tipo)
                .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePrivate())
                .body(new InputStreamResource(in));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> invalido(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> noEncontrada(EntityNotFoundException e) {
        return ResponseEntity.status(404).body(Map.of("mensaje", e.getMessage()));
    }
}
