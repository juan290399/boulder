package com.boulder.boulder.controllers;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.boulder.boulder.dto.AvanceOperativoDTO;
import com.boulder.boulder.services.AvanceOperativoService;

import jakarta.validation.Valid;

/**
 * Superficie REST común de los partes de avance operativo (CU12 a CU18).
 *
 * <p>
 * Los siete partes exponen exactamente los mismos cinco verbos sobre rutas
 * distintas. Declararlos aquí una vez garantiza que todos filtren igual, que
 * todos devuelvan los mismos códigos y que un cambio de contrato no deje a uno
 * de ellos atrás.
 * </p>
 *
 * <p>
 * El {@code @RequestBody} está tipado con la variable {@code D}. Spring la
 * resuelve contra la clase concreta del controlador, así que cada subclase
 * deserializa su propio DTO sin necesidad de redeclarar el método. La clase no
 * lleva {@code @RestController} ni {@code @RequestMapping} a propósito: si los
 * llevara, Spring intentaría registrarla como endpoint por sí misma.
 * </p>
 *
 * @param <D> DTO concreto del parte
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
public abstract class AvanceOperativoControllerBase<D extends AvanceOperativoDTO> {

    private static final Logger log = LoggerFactory.getLogger(AvanceOperativoControllerBase.class);

    /** Servicio del parte concreto. */
    protected abstract AvanceOperativoService<D> servicio();

    /** Nombre legible del parte; solo se usa en los logs. */
    protected abstract String nombreParte();

    /**
     * Lista los partes, con filtros opcionales y excluyentes por orden de
     * precedencia: sondaje, rango de fechas, fecha suelta.
     */
    @GetMapping
    public ResponseEntity<List<D>> listar(
            @RequestParam(value = "sondajeId", required = false) UUID sondajeId,
            @RequestParam(value = "fecha", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(value = "desde", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(value = "hasta", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        if (sondajeId != null) {
            log.info("HTTP GET - Partes de {} por sondaje: {}", nombreParte(), sondajeId);
            return ResponseEntity.ok(servicio().listarPorSondaje(sondajeId));
        }
        if (desde != null && hasta != null) {
            log.info("HTTP GET - Partes de {} entre {} y {}", nombreParte(), desde, hasta);
            return ResponseEntity.ok(servicio().listarPorRango(desde, hasta));
        }
        if (fecha != null) {
            log.info("HTTP GET - Partes de {} por fecha: {}", nombreParte(), fecha);
            return ResponseEntity.ok(servicio().listarPorFecha(fecha));
        }
        log.info("HTTP GET - Listado de partes de {}", nombreParte());
        return ResponseEntity.ok(servicio().listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<D> obtenerPorId(@PathVariable UUID id) {
        log.info("HTTP GET - Parte de {} ID: {}", nombreParte(), id);
        return ResponseEntity.ok(servicio().obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<D> guardar(@Valid @RequestBody D dto) {
        log.info("HTTP POST - Nuevo parte de {}", nombreParte());
        return new ResponseEntity<>(servicio().guardar(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<D> actualizar(@PathVariable UUID id, @Valid @RequestBody D dto) {
        log.info("HTTP PUT - Parte de {} ID: {}", nombreParte(), id);
        return ResponseEntity.ok(servicio().actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        log.warn("HTTP DELETE - Parte de {} ID: {}", nombreParte(), id);
        servicio().eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
