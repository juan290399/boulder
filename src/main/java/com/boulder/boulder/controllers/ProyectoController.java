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

import com.boulder.boulder.dto.ProyectoDTO;
import com.boulder.boulder.services.ProyectoService;

/**
 * Controlador REST para la gestión de Proyectos Mineros.
 * Expone los endpoints necesarios para la interacción con el cliente Angular.
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/proyecto")
@CrossOrigin(origins = "*") // Ajustar en producción a la URL específica de Angular
public class ProyectoController {

    private static final Logger log = LoggerFactory.getLogger(ProyectoController.class);

    @Autowired
    private ProyectoService proyectoService;

    @GetMapping
    public ResponseEntity<List<ProyectoDTO>> listarTodos() {
        log.info("HTTP GET - Solicitud para listar todos los proyectos");
        List<ProyectoDTO> proyectos = proyectoService.listarTodos();
        return ResponseEntity.ok(proyectos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProyectoDTO> obtenerPorId(@PathVariable UUID id) {
        log.info("HTTP GET - Buscando proyecto con ID: {}", id);
        ProyectoDTO proyecto = proyectoService.obtenerPorId(id);
        return ResponseEntity.ok(proyecto);
    }

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<ProyectoDTO> obtenerPorCodigo(@PathVariable String codigo) {
        log.info("HTTP GET - Buscando proyecto con código: {}", codigo);
        ProyectoDTO proyecto = proyectoService.obtenerPorCodigo(codigo);
        return ResponseEntity.ok(proyecto);
    }

    @PostMapping
    public ResponseEntity<ProyectoDTO> guardar(@RequestBody ProyectoDTO dto) {
        log.info("HTTP POST - Intentando registrar nuevo proyecto con código: {}", dto.getCodigo());
        ProyectoDTO guardado = proyectoService.guardar(dto);
        return new ResponseEntity<>(guardado, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProyectoDTO> actualizar(@PathVariable UUID id, @RequestBody ProyectoDTO dto) {
        log.info("HTTP PUT - Solicitud de actualización para el proyecto ID: {}", id);
        ProyectoDTO actualizado = proyectoService.actualizar(id, dto);
        return ResponseEntity.ok(actualizado);
    }
}