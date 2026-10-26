package com.boulder.boulder.controllers;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boulder.boulder.entities.Alteracion;
import com.boulder.boulder.entities.Estructural;
import com.boulder.boulder.entities.Litologia;
import com.boulder.boulder.entities.Mineralizacion;
import com.boulder.boulder.entities.Quicklog;
import com.boulder.boulder.services.LogueoService;

/**
 * Controlador REST del logueo geológico.
 *
 * <p>Es la cara del esquema {@code logueo} hacia la aplicación de campo. Cinco
 * plantillas, dos operaciones cada una: {@code GET} con el sondaje para bajar lo
 * que ya está registrado y {@code PUT} con un lote para subir lo que se logueó
 * sin cobertura.</p>
 *
 * <p><b>PUT y no POST.</b> El identificador lo genera el tablet antes de tener
 * red, así que la operación es idempotente por naturaleza. Con POST habría que
 * decidir qué hacer cuando un reintento reenvía algo que ya se guardó, que es
 * precisamente el caso normal cuando la cobertura va y viene en una plataforma
 * de sondaje.</p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/logueo")
@CrossOrigin(origins = "*")
public class LogueoController {

    private static final Logger log = LoggerFactory.getLogger(LogueoController.class);

    @Autowired
    private LogueoService logueoService;

    // ------------------------------------------------------------------
    // Bajada
    // ------------------------------------------------------------------

    @GetMapping("/litologia")
    public ResponseEntity<List<Litologia>> litologia(@RequestParam UUID sondajeId,
            @RequestParam(defaultValue = "false") boolean incluirEliminados) {
        log.info("HTTP GET - Litología del sondaje {}", sondajeId);
        return ResponseEntity.ok(logueoService.listarLitologia(sondajeId, incluirEliminados));
    }

    @GetMapping("/alteracion")
    public ResponseEntity<List<Alteracion>> alteracion(@RequestParam UUID sondajeId,
            @RequestParam(defaultValue = "false") boolean incluirEliminados) {
        log.info("HTTP GET - Alteración del sondaje {}", sondajeId);
        return ResponseEntity.ok(logueoService.listarAlteracion(sondajeId, incluirEliminados));
    }

    @GetMapping("/mineralizacion")
    public ResponseEntity<List<Mineralizacion>> mineralizacion(@RequestParam UUID sondajeId,
            @RequestParam(defaultValue = "false") boolean incluirEliminados) {
        log.info("HTTP GET - Mineralización del sondaje {}", sondajeId);
        return ResponseEntity.ok(logueoService.listarMineralizacion(sondajeId, incluirEliminados));
    }

    @GetMapping("/estructural")
    public ResponseEntity<List<Estructural>> estructural(@RequestParam UUID sondajeId,
            @RequestParam(defaultValue = "false") boolean incluirEliminados) {
        log.info("HTTP GET - Estructural del sondaje {}", sondajeId);
        return ResponseEntity.ok(logueoService.listarEstructural(sondajeId, incluirEliminados));
    }

    @GetMapping("/quicklog")
    public ResponseEntity<List<Quicklog>> quicklog(@RequestParam UUID sondajeId,
            @RequestParam(defaultValue = "false") boolean incluirEliminados) {
        log.info("HTTP GET - Quicklog del sondaje {}", sondajeId);
        return ResponseEntity.ok(logueoService.listarQuicklog(sondajeId, incluirEliminados));
    }

    // ------------------------------------------------------------------
    // Subida
    // ------------------------------------------------------------------

    @PutMapping("/litologia")
    public ResponseEntity<Void> subirLitologia(@RequestBody List<Litologia> items,
            @RequestHeader(value = "X-Dispositivo", required = false) String dispositivo,
            @RequestHeader(value = "X-App-Version", required = false) String versionApp,
            Principal principal) {
        logueoService.guardarLitologia(items, dispositivo, principal != null ? principal.getName() : null, versionApp);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/alteracion")
    public ResponseEntity<Void> subirAlteracion(@RequestBody List<Alteracion> items,
            @RequestHeader(value = "X-Dispositivo", required = false) String dispositivo,
            @RequestHeader(value = "X-App-Version", required = false) String versionApp,
            Principal principal) {
        logueoService.guardarAlteracion(items, dispositivo, principal != null ? principal.getName() : null, versionApp);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/mineralizacion")
    public ResponseEntity<Void> subirMineralizacion(@RequestBody List<Mineralizacion> items,
            @RequestHeader(value = "X-Dispositivo", required = false) String dispositivo,
            @RequestHeader(value = "X-App-Version", required = false) String versionApp,
            Principal principal) {
        logueoService.guardarMineralizacion(items, dispositivo, principal != null ? principal.getName() : null, versionApp);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/estructural")
    public ResponseEntity<Void> subirEstructural(@RequestBody List<Estructural> items,
            @RequestHeader(value = "X-Dispositivo", required = false) String dispositivo,
            @RequestHeader(value = "X-App-Version", required = false) String versionApp,
            Principal principal) {
        logueoService.guardarEstructural(items, dispositivo, principal != null ? principal.getName() : null, versionApp);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/quicklog")
    public ResponseEntity<Void> subirQuicklog(@RequestBody List<Quicklog> items,
            @RequestHeader(value = "X-Dispositivo", required = false) String dispositivo,
            @RequestHeader(value = "X-App-Version", required = false) String versionApp,
            Principal principal) {
        logueoService.guardarQuicklog(items, dispositivo, principal != null ? principal.getName() : null, versionApp);
        return ResponseEntity.noContent().build();
    }

    /** Tablets de GeoLog y cuándo sincronizó cada una por última vez. */
    @GetMapping("/dispositivos")
    public ResponseEntity<List<java.util.Map<String, Object>>> dispositivos() {
        return ResponseEntity.ok(logueoService.dispositivos());
    }
}
