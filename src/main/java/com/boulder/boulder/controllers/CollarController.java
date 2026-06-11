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

import com.boulder.boulder.dto.CollarDTO;
import com.boulder.boulder.services.CollarService;

/**
 * Controlador REST para el control de puntos Collar (Bocas de pozo validadas).
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/collar")
@CrossOrigin(origins = "*")
public class CollarController {

    private static final Logger log = LoggerFactory.getLogger(CollarController.class);

    @Autowired
    private CollarService collarService;

    @GetMapping
    public ResponseEntity<List<CollarDTO>> listarTodos() {
        log.info("HTTP GET - Solicitud de listado maestro de collares");
        return ResponseEntity.ok(collarService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CollarDTO> obtenerPorId(@PathVariable UUID id) {
        log.info("HTTP GET - Obteniendo collar ID: {}", id);
        return ResponseEntity.ok(collarService.obtenerPorId(id));
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<CollarDTO> obtenerPorCodigo(@PathVariable String codigo) {
        log.info("HTTP GET - Obteniendo collar codigo: {}", codigo);
        return ResponseEntity.ok(collarService.obtenerPorCodigo(codigo));
    }

    @PostMapping
    public ResponseEntity<CollarDTO> guardar(@RequestBody CollarDTO dto) {
        log.info("HTTP POST - Registrando punto de collar código: {}", dto.getCodigo());
        CollarDTO guardado = collarService.guardar(dto);
        return new ResponseEntity<>(guardado, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CollarDTO> actualizar(@PathVariable UUID id, @RequestBody CollarDTO dto) {
        log.info("HTTP PUT - Solicitud de edición topográfica para el Collar ID: {}", id);
        return ResponseEntity.ok(collarService.actualizar(id, dto));
    }
}