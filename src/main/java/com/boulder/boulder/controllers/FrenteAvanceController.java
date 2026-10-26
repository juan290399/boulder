package com.boulder.boulder.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boulder.boulder.dto.FrenteAvanceDTO;
import com.boulder.boulder.services.FrenteAvanceService;

/**
 * Controlador REST del frente de avance por sondaje.
 *
 * <p>
 * Es la consulta que alimenta la pantalla principal del geólogo: un renglón
 * por sondaje con la profundidad alcanzada por cada etapa del flujo.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/operacional/frente-avance")
@CrossOrigin(origins = "*")
public class FrenteAvanceController {

    private static final Logger log = LoggerFactory.getLogger(FrenteAvanceController.class);

    @Autowired
    private FrenteAvanceService frenteAvanceService;

    @GetMapping
    public ResponseEntity<List<FrenteAvanceDTO>> consultar(
            @RequestParam(value = "soloActivos", defaultValue = "true") boolean soloActivos) {
        log.info("HTTP GET - Frente de avance. soloActivos={}", soloActivos);
        return ResponseEntity.ok(frenteAvanceService.consultar(soloActivos));
    }
}
