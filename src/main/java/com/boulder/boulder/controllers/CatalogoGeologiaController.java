package com.boulder.boulder.controllers;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boulder.boulder.dto.CatalogoGeologiaDTO;
import com.boulder.boulder.services.CatalogoGeologiaService;

/**
 * Controlador REST de los diccionarios del esquema {@code geologia}.
 *
 * <p>La lectura es un solo endpoint, y a propósito: la aplicación de campo los
 * descarga enteros al sincronizar para poder loguear después sin cobertura. Son
 * unas 350 filas; partirlas por catálogo serían veintiún viajes de red desde una
 * plataforma de sondaje para traer lo mismo.</p>
 *
 * <p>La escritura (pantalla P15, RF04) es solo para el rol ADMIN: un código mal
 * dado de alta se ofrece a todos los geólogos en todos los tablets.</p>
 *
 * @author jvelazco
 * @version 1.1
 * @since 1.0
 */
@RestController
@RequestMapping("/api/geologia/catalogos")
public class CatalogoGeologiaController {

    private static final Logger log = LoggerFactory.getLogger(CatalogoGeologiaController.class);

    private final CatalogoGeologiaService catalogoService;

    public CatalogoGeologiaController(CatalogoGeologiaService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping
    public ResponseEntity<List<CatalogoGeologiaDTO>> listarTodos() {
        log.info("HTTP GET - Descarga completa de catálogos geológicos");
        return ResponseEntity.ok(catalogoService.listarTodos());
    }

    /**
     * Agrega un código al catálogo indicado.
     *
     * @param tipo nombre del catálogo, p. ej. {@code lit_diccionario}
     * @return 201 con la entrada creada; 400 si no cumple las reglas; 409 si el código ya existe
     */
    @PostMapping("/{tipo}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CatalogoGeologiaDTO> crear(@PathVariable String tipo, @RequestBody CatalogoGeologiaDTO datos) {
        return new ResponseEntity<>(catalogoService.crear(tipo, datos), HttpStatus.CREATED);
    }

    /** Corrige una entrada existente; 404 si no existe en ese catálogo. */
    @PutMapping("/{tipo}/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public CatalogoGeologiaDTO actualizar(@PathVariable String tipo, @PathVariable UUID id,
                                          @RequestBody CatalogoGeologiaDTO datos) {
        return catalogoService.actualizar(tipo, id, datos);
    }

    /** Activa o desactiva una entrada sin borrarla. */
    @PatchMapping("/{tipo}/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> cambiarEstado(@PathVariable String tipo, @PathVariable UUID id,
                                              @RequestParam boolean activo) {
        catalogoService.cambiarEstado(tipo, id, activo);
        return ResponseEntity.noContent().build();
    }
}
