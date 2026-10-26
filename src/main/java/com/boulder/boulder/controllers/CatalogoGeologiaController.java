package com.boulder.boulder.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.boulder.boulder.dto.CatalogoGeologiaDTO;
import com.boulder.boulder.services.CatalogoGeologiaService;

/**
 * Controlador REST de los diccionarios del esquema {@code geologia}.
 *
 * <p>Un solo endpoint, y a propósito: la aplicación de campo los descarga
 * enteros al sincronizar para poder loguear después sin cobertura. Son 348
 * filas; partirlas por catálogo serían veintiún viajes de red desde una
 * plataforma de sondaje para traer lo mismo.</p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/geologia/catalogos")
@CrossOrigin(origins = "*")
public class CatalogoGeologiaController {

    private static final Logger log = LoggerFactory.getLogger(CatalogoGeologiaController.class);

    @Autowired
    private CatalogoGeologiaService catalogoService;

    @GetMapping
    public ResponseEntity<List<CatalogoGeologiaDTO>> listarTodos() {
        log.info("HTTP GET - Descarga completa de catálogos geológicos");
        return ResponseEntity.ok(catalogoService.listarTodos());
    }
}
