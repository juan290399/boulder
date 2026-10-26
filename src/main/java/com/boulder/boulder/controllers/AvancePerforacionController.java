package com.boulder.boulder.controllers;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boulder.boulder.dto.AvancePerforacionDTO;
import com.boulder.boulder.services.AvancePerforacionService;

import jakarta.validation.Valid;

/**
 * Controlador REST para los partes diarios de avance de perforación (UC15).
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/operacional/avances-perforacion")
public class AvancePerforacionController {

    private static final Logger log = LoggerFactory.getLogger(AvancePerforacionController.class);

    @Autowired
    private AvancePerforacionService avanceService;

    @GetMapping
    public ResponseEntity<List<AvancePerforacionDTO>> listar(
            @RequestParam(value = "sondajeId", required = false) UUID sondajeId,
            @RequestParam(value = "fecha", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        if (sondajeId != null) {
            log.info("HTTP GET - Partes de avance por sondaje: {}", sondajeId);
            return ResponseEntity.ok(avanceService.listarPorSondaje(sondajeId));
        }
        if (fecha != null) {
            log.info("HTTP GET - Partes de avance por fecha: {}", fecha);
            return ResponseEntity.ok(avanceService.listarPorFecha(fecha));
        }
        log.info("HTTP GET - Listado de partes de avance de perforación");
        return ResponseEntity.ok(avanceService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AvancePerforacionDTO> obtenerPorId(@PathVariable UUID id) {
        log.info("HTTP GET - Parte de avance ID: {}", id);
        return ResponseEntity.ok(avanceService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<AvancePerforacionDTO> guardar(@Valid @RequestBody AvancePerforacionDTO dto) {
        log.info("HTTP POST - Nuevo parte de avance de perforación");
        return new ResponseEntity<>(avanceService.guardar(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AvancePerforacionDTO> actualizar(@PathVariable UUID id,
            @Valid @RequestBody AvancePerforacionDTO dto) {
        log.info("HTTP PUT - Parte de avance ID: {}", id);
        return ResponseEntity.ok(avanceService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        log.warn("HTTP DELETE - Parte de avance ID: {}", id);
        avanceService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
