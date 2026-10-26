package com.boulder.boulder.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.boulder.boulder.dto.RolDTO;
import com.boulder.boulder.services.RolService;

/**
 * Controlador REST para la consulta del catálogo de roles del sistema.
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/base/roles")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class RolController {

    private static final Logger log = LoggerFactory.getLogger(RolController.class);

    @Autowired
    private RolService rolService;

    @GetMapping
    public ResponseEntity<List<RolDTO>> listarTodos() {
        log.info("HTTP GET - Solicitud del catálogo de roles");
        return ResponseEntity.ok(rolService.listarTodos());
    }
}
