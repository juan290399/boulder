package com.boulder.boulder.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boulder.boulder.dto.SondajeDTO;
import com.boulder.boulder.services.SondajeService;

@RestController
@RequestMapping("/api/operacional/sondajes")
@CrossOrigin(origins = "*")
public class SondajeController {

    @Autowired
    private SondajeService sondajeService;

    @GetMapping("/proyecto/{proyectoId}")
    public ResponseEntity<List<SondajeDTO>> listarPorProyecto(@PathVariable UUID proyectoId) {
        return ResponseEntity.ok(sondajeService.listarPorProyecto(proyectoId));
    }

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<SondajeDTO> obtenerPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(sondajeService.obtenerPorCodigo(codigo));
    }

    @PostMapping
    public ResponseEntity<SondajeDTO> crear(@RequestBody SondajeDTO sondajeDTO) {
        return new ResponseEntity<>(sondajeService.guardar(sondajeDTO), HttpStatus.CREATED);
    }

    /**
     * Endpoint parcial (PATCH) para actualizar la profundidad medida en terreno (cambio de turno).
     * Evita enviar todo el JSON del sondaje innecesariamente.
     */
    @PatchMapping("/{id}/avance")
    public ResponseEntity<SondajeDTO> actualizarAvance(
            @PathVariable UUID id, 
            @RequestParam Double nuevaProfundidad) {
        return ResponseEntity.ok(sondajeService.actualizarAvance(id, nuevaProfundidad));
    }
}