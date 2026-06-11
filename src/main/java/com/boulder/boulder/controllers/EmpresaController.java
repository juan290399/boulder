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

import com.boulder.boulder.dto.EmpresaDTO;
import com.boulder.boulder.services.EmpresaService;

/**
 * Controlador REST para el mantenimiento de Empresas.
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/empresa")
@CrossOrigin(origins = "*")
public class EmpresaController {

    private static final Logger log = LoggerFactory.getLogger(EmpresaController.class);

    @Autowired
    private EmpresaService empresaService;

    @GetMapping
    public ResponseEntity<List<EmpresaDTO>> listarTodas() {
        log.info("HTTP GET - Solicitud de listado completo de empresas del sistema");
        return ResponseEntity.ok(empresaService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmpresaDTO> obtenerPorId(@PathVariable UUID id) {
        log.info("HTTP GET - Buscando ficha de empresa por ID: {}", id);
        return ResponseEntity.ok(empresaService.obtenerPorId(id));
    }

    @GetMapping("/ruc/{ruc}")
    public ResponseEntity<EmpresaDTO> obtenerPorRuc(@PathVariable String ruc) {
        log.info("HTTP GET - Buscando empresa por RUC/TaxID: {}", ruc);
        return ResponseEntity.ok(empresaService.obtenerPorRuc(ruc));
    }

    @PostMapping
    public ResponseEntity<EmpresaDTO> guardar(@RequestBody EmpresaDTO dto) {
        log.info("HTTP POST - Solicitud para registrar la empresa: {}", dto.getNombre());
        EmpresaDTO guardada = empresaService.guardar(dto);
        return new ResponseEntity<>(guardada, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmpresaDTO> actualizar(@PathVariable UUID id, @RequestBody EmpresaDTO dto) {
        log.info("HTTP PUT - Solicitud de actualización para la empresa ID: {}", id);
        return ResponseEntity.ok(empresaService.actualizar(id, dto));
    }
}