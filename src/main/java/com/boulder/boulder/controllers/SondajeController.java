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

import com.boulder.boulder.dto.SondajeDTO;
import com.boulder.boulder.services.SondajeService;

/**
 * Controlador REST para la gestión operativa de pozos de Sondaje perforados.
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/sondaje")
@CrossOrigin(origins = "*")
public class SondajeController {

    private static final Logger log = LoggerFactory.getLogger(SondajeController.class);

    @Autowired
    private SondajeService sondajeService;

    @GetMapping
    public ResponseEntity<List<SondajeDTO>> listarTodos() {
        log.info("HTTP GET - Solicitud de listado completo de sondajes activos");
        return ResponseEntity.ok(sondajeService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SondajeDTO> obtenerPorId(@PathVariable UUID id) {
        log.info("HTTP GET - Consultando sondaje individual ID: {}", id);
        return ResponseEntity.ok(sondajeService.obtenerPorId(id));
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<SondajeDTO> obtenerPorCodigo(@PathVariable String codigo) {
        log.info("HTTP GET - Consultando sondaje individual por codigo: {}", codigo);
        return ResponseEntity.ok(sondajeService.obtenerPorCodigo(codigo));
    }

    @GetMapping("/{collarId}")
    public ResponseEntity<SondajeDTO> obtenerPorCollar(@PathVariable UUID collarId) {
        log.info("HTTP GET - Recuperando sondajes ejecutados en la Collar ID: {}", collarId);
        return ResponseEntity.ok(sondajeService.obtenerPorCollar(collarId));
    }

    @GetMapping("/proyecto/{proyectoId}")
    public ResponseEntity<List<SondajeDTO>> listarPorProyecto(@PathVariable UUID proyectoId) {
        log.info("HTTP GET - Recuperando sondajes ejecutados en la Proyecto ID: {}", proyectoId);
        return ResponseEntity.ok(sondajeService.listarPorProyecto(proyectoId));
    }

    @GetMapping("/maquina/{maquinaId}")
    public ResponseEntity<List<SondajeDTO>> listarPorMaquina(@PathVariable UUID maquinaId) {
        log.info("HTTP GET - Recuperando sondajes ejecutados por la maquina ID: {}", maquinaId);
        return ResponseEntity.ok(sondajeService.listarPorMaquina(maquinaId));
    }

    @GetMapping("/plataforma/{plataformaId}")
    public ResponseEntity<List<SondajeDTO>> listarPorPlataforma(@PathVariable UUID plataformaId) {
        log.info("HTTP GET - Recuperando sondajes ejecutados en la Plataforma ID: {}", plataformaId);
        return ResponseEntity.ok(sondajeService.listarPorPlataforma(plataformaId));
    }

    
    @PostMapping
    public ResponseEntity<SondajeDTO> guardar(@RequestBody SondajeDTO dto) {
        log.info("HTTP POST - Iniciando registro de perforación física para el pozo: {}", dto.getCodigo());
        SondajeDTO guardado = sondajeService.guardar(dto);
        return new ResponseEntity<>(guardado, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SondajeDTO> actualizar(@PathVariable UUID id, @RequestBody SondajeDTO dto) {
        log.info("HTTP PUT - Actualizando parámetros operacionales del sondaje ID: {}", id);
        return ResponseEntity.ok(sondajeService.actualizar(id, dto));
    }

}