package com.boulder.boulder.controllers;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.boulder.boulder.dto.SurveyDTO;
import com.boulder.boulder.services.SurveyService;

/**
 * Controlador REST para el control de trayectorias y Surveys de pozos.
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/survey")
@CrossOrigin(origins = "*")
public class SurveyController {

    private static final Logger log = LoggerFactory.getLogger(SurveyController.class);

    @Autowired
    private SurveyService surveyService;

    @GetMapping
    public ResponseEntity<List<SurveyDTO>> listarTodos() {
        log.info("HTTP GET - Solicitud del histórico total de mediciones survey");
        return ResponseEntity.ok(surveyService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SurveyDTO> obtenerPorId(@PathVariable UUID id) {
        log.info("HTTP GET - Buscando estación survey ID: {}", id);
        return ResponseEntity.ok(surveyService.obtenerPorId(id));
    }

    @GetMapping("/sondaje/{sondajeId}")
    public ResponseEntity<List<SurveyDTO>> listarPorSondaje(@PathVariable UUID sondajeId) {
        log.info("HTTP GET - Extrayendo trayectoria secuencial del Sondaje ID: {}", sondajeId);
        return ResponseEntity.ok(surveyService.listarPorSondaje(sondajeId));
    }

    @PostMapping
    public ResponseEntity<SurveyDTO> guardar(@RequestBody SurveyDTO dto) {
        log.info("HTTP POST - Registrando punto de survey a {}m en Sondaje ID: {}", dto.getProfundidad(), dto.getSondajeId());
        SurveyDTO guardado = surveyService.guardar(dto);
        return new ResponseEntity<>(guardado, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SurveyDTO> actualizar(@PathVariable UUID id, @RequestBody SurveyDTO dto) {
        log.info("HTTP PUT - Modificando datos de calibración para el Survey ID: {}", id);
        return ResponseEntity.ok(surveyService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        log.warn("HTTP DELETE - Eliminando estación survey ID: {}", id);
        surveyService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}