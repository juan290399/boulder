package com.boulder.boulder.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.boulder.boulder.dto.importacion.PlantillaDTO;
import com.boulder.boulder.dto.importacion.ResultadoImportacionDTO;
import com.boulder.boulder.services.ImportacionService;
import com.boulder.boulder.services.importacion.ImportacionAbortadaException;

/**
 * Carga masiva de las plantillas de Excel.
 *
 * <p>
 * Dos verbos y nada más: uno lista las plantillas que el sistema sabe leer y
 * el otro recibe un archivo. La diferencia entre probar y cargar es un
 * parámetro, {@code aplicar}, que por omisión vale falso.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/importacion")
public class ImportacionController {

    @Autowired
    private ImportacionService importacionService;

    /** Las plantillas disponibles, para que la pantalla arme el desplegable. */
    @GetMapping("/plantillas")
    public ResponseEntity<List<PlantillaDTO>> plantillas() {
        return ResponseEntity.ok(importacionService.plantillas());
    }

    /**
     * Recibe un libro y devuelve el informe de lo que se cargó y lo que no.
     *
     * @param aplicar {@code false} —el valor por omisión— simula: valida todo y
     *                no escribe nada
     */
    @PostMapping("/{plantilla}")
    public ResponseEntity<ResultadoImportacionDTO> importar(
            @PathVariable("plantilla") String plantilla,
            @RequestPart("archivo") MultipartFile archivo,
            @RequestParam(name = "aplicar", defaultValue = "false") boolean aplicar) {
        return ResponseEntity.ok(importacionService.importar(plantilla, archivo, aplicar));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> argumentoInvalido(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
    }

    /**
     * La base rechazó un renglón que la validación dejó pasar. No se cargó
     * nada; el mensaje dice en qué fila fue.
     */
    @ExceptionHandler(ImportacionAbortadaException.class)
    public ResponseEntity<Map<String, Object>> abortada(ImportacionAbortadaException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "mensaje", e.getMessage(),
                "hoja", e.getHoja(),
                "fila", e.getFila(),
                "cargado", false));
    }
}
