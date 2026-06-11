package com.boulder.boulder.controllers;

import java.util.List;
import java.util.UUID;

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

import com.boulder.boulder.dto.MaquinaDTO;
import com.boulder.boulder.services.MaquinaService;

@RestController
@RequestMapping("/maquina")
@CrossOrigin(origins = "*")
public class MaquinaController {

    @Autowired
    private MaquinaService maquinaService;

    @GetMapping
    public ResponseEntity<List<MaquinaDTO>> listarTodas() {
        return ResponseEntity.ok(maquinaService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaquinaDTO> obtenerPorId(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(maquinaService.obtenerPorId(id));
    }

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<MaquinaDTO> obtenerPorCodigo(@PathVariable("codigo") String codigo) {
        return ResponseEntity.ok(maquinaService.obtenerPorCodigo(codigo));
    }

    @PostMapping
    public ResponseEntity<MaquinaDTO> guardar(@RequestBody MaquinaDTO maquinaDTO) {
        return new ResponseEntity<>(
                maquinaService.guardar(maquinaDTO),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<MaquinaDTO> actualizar(
            @PathVariable("id") UUID id,
            @RequestBody MaquinaDTO maquinaDTO) {

        return ResponseEntity.ok(
                maquinaService.actualizar(id, maquinaDTO)
        );
    }
}