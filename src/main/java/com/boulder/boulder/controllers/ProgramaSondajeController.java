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

import com.boulder.boulder.dto.ProgramaSondajeDTO;
import com.boulder.boulder.services.ProgramaSondajeService;

/**
 * Controlador REST para el control de Programas de Sondaje (Campañas teóricas).
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/programa-sondaje")
@CrossOrigin(origins = "*")
public class ProgramaSondajeController {

    private static final Logger log = LoggerFactory.getLogger(ProgramaSondajeController.class);

    @Autowired
    private ProgramaSondajeService programaService;

    @GetMapping
    public ResponseEntity<List<ProgramaSondajeDTO>> listarTodos() {
        log.info("HTTP GET - Listar todos los programas de sondaje");
        return ResponseEntity.ok(programaService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProgramaSondajeDTO> obtenerPorId(@PathVariable UUID id) {
        log.info("HTTP GET - Obteniendo programa de sondaje por ID: {}", id);
        return ResponseEntity.ok(programaService.obtenerPorId(id));
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<ProgramaSondajeDTO> obtenerPorCodigo(@PathVariable String codigo) {
        log.info("HTTP GET - Obteniendo programa de sondaje por codigo: {}", codigo);
        return ResponseEntity.ok(programaService.obtenerPorCodigo(codigo));
    }

    @GetMapping("/proyecto/{proyectoId}")
    public ResponseEntity<List<ProgramaSondajeDTO>> listarPorProyecto(@PathVariable UUID proyectoId) {
        log.info("HTTP GET - Consultando planes del Proyecto ID: {}", proyectoId);
        return ResponseEntity.ok(programaService.listarPorProyecto(proyectoId));
    }

    @GetMapping("/plataforma/{plataformaId}")
    public ResponseEntity<List<ProgramaSondajeDTO>> listarPorPlataforma(@PathVariable UUID plataformaId) {
        log.info("HTTP GET - Consultando planes del Proyecto ID: {}", plataformaId);
        return ResponseEntity.ok(programaService.listarPorPlataforma(plataformaId));
    }

    @PostMapping
    public ResponseEntity<ProgramaSondajeDTO> guardar(@RequestBody ProgramaSondajeDTO dto) {
        log.info("HTTP POST - Creando plan de perforación: {}", dto.getCodigo());
        ProgramaSondajeDTO guardado = programaService.guardar(dto);
        return new ResponseEntity<>(guardado, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProgramaSondajeDTO> actualizar(@PathVariable UUID id, @RequestBody ProgramaSondajeDTO dto) {
        log.info("HTTP PUT - Actualizando programa de sondaje ID: {}", id);
        return ResponseEntity.ok(programaService.actualizar(id, dto));
    }
}