package com.boulder.boulder.controllers;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boulder.boulder.dto.CargoDTO;
import com.boulder.boulder.dto.EmpleadoDTO;
import com.boulder.boulder.services.EmpleadoService;

import jakarta.validation.Valid;

/**
 * Controlador REST para el catálogo de personal.
 *
 * <p>
 * Sustituye al antiguo {@code /api/operacional/perforistas}, que solo cubría la
 * dotación de perforación.
 * </p>
 *
 * <p>
 * Las escrituras son exclusivas de ADMIN; las lecturas quedan abiertas a
 * cualquier sesión porque los partes de perforación y de logueo necesitan
 * poblar sus desplegables de personal. La excepción de lectura está declarada
 * en {@code SecurityConfig}, ya que {@code /api/base/**} es ADMIN por defecto.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/base/empleados")
public class EmpleadoController {

    private static final Logger log = LoggerFactory.getLogger(EmpleadoController.class);

    @Autowired
    private EmpleadoService empleadoService;

    /**
     * Catálogo de cargos.
     *
     * <p>
     * Declarado antes que {@code /{id}} por legibilidad; Spring resuelve las
     * rutas literales antes que las plantillas, así que "cargos" no se intenta
     * convertir a UUID.
     * </p>
     */
    @GetMapping("/cargos")
    public ResponseEntity<List<CargoDTO>> listarCargos() {
        log.info("HTTP GET - Catálogo de cargos");
        return ResponseEntity.ok(empleadoService.listarCargos());
    }

    /**
     * Listado de personal. Con {@code ?area=perforacion} devuelve solo los
     * activos de esa área, que es lo que necesita un desplegable.
     */
    @GetMapping
    public ResponseEntity<List<EmpleadoDTO>> listar(@RequestParam(required = false) String area) {
        if (area != null && !area.isBlank()) {
            log.info("HTTP GET - Personal activo del área: {}", area);
            return ResponseEntity.ok(empleadoService.listarPorArea(area));
        }
        log.info("HTTP GET - Catálogo completo de personal");
        return ResponseEntity.ok(empleadoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmpleadoDTO> obtenerPorId(@PathVariable UUID id) {
        log.info("HTTP GET - Empleado ID: {}", id);
        return ResponseEntity.ok(empleadoService.obtenerPorId(id));
    }

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<EmpleadoDTO> obtenerPorCodigo(@PathVariable String codigo) {
        log.info("HTTP GET - Empleado código: {}", codigo);
        return ResponseEntity.ok(empleadoService.obtenerPorCodigo(codigo));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<EmpleadoDTO> guardar(@Valid @RequestBody EmpleadoDTO dto) {
        log.info("HTTP POST - Nuevo empleado código: {}", dto.getCodigo());
        return new ResponseEntity<>(empleadoService.guardar(dto), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<EmpleadoDTO> actualizar(@PathVariable UUID id, @Valid @RequestBody EmpleadoDTO dto) {
        log.info("HTTP PUT - Empleado ID: {}", id);
        return ResponseEntity.ok(empleadoService.actualizar(id, dto));
    }

    /**
     * Alta o baja del empleado.
     *
     * <p>
     * No hay DELETE: los partes de perforación y las mediciones de desviación
     * referencian estas filas, y borrarlas dejaría los registros históricos sin
     * autor identificable.
     * </p>
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstado(@PathVariable UUID id, @RequestParam boolean activo) {
        log.warn("HTTP PATCH - Empleado ID: {} -> activo: {}", id, activo);
        empleadoService.cambiarEstado(id, activo);
        return ResponseEntity.noContent().build();
    }
}
