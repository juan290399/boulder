package com.boulder.boulder.controllers;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.boulder.boulder.dto.PlataformaDTO;
import com.boulder.boulder.services.PlataformaService;

/**
 * Controlador REST para la administración de Plataformas físicas de perforación.
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/plataforma")
@CrossOrigin(origins = "*")
public class PlataformaController {

    private static final Logger log = LoggerFactory.getLogger(PlataformaController.class);

    @Autowired
    private PlataformaService plataformaService;

    @GetMapping
    public ResponseEntity<List<PlataformaDTO>> listarTodas() {
        log.info("HTTP GET - Consultando catálogo de plataformas");
        return ResponseEntity.ok(plataformaService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlataformaDTO> obtenerPorId(@PathVariable UUID id) {
        log.info("HTTP GET - Buscando plataforma por ID: {}", id);
        return ResponseEntity.ok(plataformaService.obtenerPorId(id));
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<PlataformaDTO> obtenerPorCodigo(@PathVariable String codigo) {
        log.info("HTTP GET - Buscando plataforma por código: {}", codigo);
        return ResponseEntity.ok(plataformaService.obtenerPorCodigo(codigo));
    }


    @GetMapping("/proyecto/{proyectoId}")
    public ResponseEntity<List<PlataformaDTO>> listarPorProyecto(@PathVariable UUID proyectoId) {
        log.info("HTTP GET - Filtrando plataformas habilitadas para el Proyecto ID: {}", proyectoId);
        return ResponseEntity.ok(plataformaService.listarPorProyecto(proyectoId));
    }

    @PostMapping
    public ResponseEntity<PlataformaDTO> guardar(@RequestBody PlataformaDTO dto) {
        log.info("HTTP POST - Registrando habilitación de plataforma: {}", dto.getCodigo());
        PlataformaDTO guardado = plataformaService.guardar(dto);
        return new ResponseEntity<>(guardado, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlataformaDTO> actualizar(@PathVariable UUID id, @RequestBody PlataformaDTO dto) {
        log.info("HTTP PUT - Modificando parámetros de la plataforma ID: {}", id);
        return ResponseEntity.ok(plataformaService.actualizar(id, dto));
    }
}