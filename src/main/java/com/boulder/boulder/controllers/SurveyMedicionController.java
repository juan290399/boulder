package com.boulder.boulder.controllers;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

import com.boulder.boulder.dto.SurveyMedicionDTO;
import com.boulder.boulder.services.SurveyMedicionService;

import jakarta.validation.Valid;

/**
 * Controlador REST para los eventos de medición de desviación de sondaje.
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/operacional/survey-mediciones")
public class SurveyMedicionController {

    private static final Logger log = LoggerFactory.getLogger(SurveyMedicionController.class);

    @Autowired
    private SurveyMedicionService medicionService;

    @GetMapping
    public ResponseEntity<List<SurveyMedicionDTO>> listar(
            @RequestParam(value = "sondajeId", required = false) UUID sondajeId) {
        if (sondajeId != null) {
            log.info("HTTP GET - Mediciones de desviación por sondaje: {}", sondajeId);
            return ResponseEntity.ok(medicionService.listarPorSondaje(sondajeId));
        }
        log.info("HTTP GET - Listado de mediciones de desviación");
        return ResponseEntity.ok(medicionService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SurveyMedicionDTO> obtenerPorId(@PathVariable UUID id) {
        log.info("HTTP GET - Medición de desviación ID: {}", id);
        return ResponseEntity.ok(medicionService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<SurveyMedicionDTO> guardar(@Valid @RequestBody SurveyMedicionDTO dto) {
        log.info("HTTP POST - Nueva medición de desviación");
        return new ResponseEntity<>(medicionService.guardar(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SurveyMedicionDTO> actualizar(@PathVariable UUID id,
            @Valid @RequestBody SurveyMedicionDTO dto) {
        log.info("HTTP PUT - Medición de desviación ID: {}", id);
        return ResponseEntity.ok(medicionService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        log.warn("HTTP DELETE - Medición de desviación ID: {}", id);
        medicionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
