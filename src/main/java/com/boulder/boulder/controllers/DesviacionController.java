package com.boulder.boulder.controllers;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boulder.boulder.dto.DesviacionMaquinaDTO;
import com.boulder.boulder.dto.DesviacionPerforistaDTO;
import com.boulder.boulder.dto.DesviacionSondajeDTO;
import com.boulder.boulder.dto.TrayectoriaSondajeDTO;
import com.boulder.boulder.services.DesviacionService;

/**
 * Controlador REST de solo lectura para la analítica de desviaciones de sondaje.
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/operacional/desviaciones")
public class DesviacionController {

    private static final Logger log = LoggerFactory.getLogger(DesviacionController.class);

    @Autowired
    private DesviacionService desviacionService;

    @GetMapping("/perforista")
    public ResponseEntity<List<DesviacionPerforistaDTO>> porPerforista() {
        log.info("HTTP GET - Resumen de desviación por perforista");
        return ResponseEntity.ok(desviacionService.resumenPorPerforista());
    }

    @GetMapping("/maquina")
    public ResponseEntity<List<DesviacionMaquinaDTO>> porMaquina() {
        log.info("HTTP GET - Resumen de desviación por máquina");
        return ResponseEntity.ok(desviacionService.resumenPorMaquina());
    }

    @GetMapping("/sondaje")
    public ResponseEntity<List<DesviacionSondajeDTO>> porSondaje(
            @RequestParam(value = "codigo", required = false) String codigo) {
        log.info("HTTP GET - Evolución de desviación por sondaje. Filtro: {}", codigo);
        return ResponseEntity.ok(desviacionService.evolucionPorSondaje(codigo));
    }

    /**
     * Trayectoria planificada contra medida, para graficar el desvío.
     *
     * <p>
     * Reemplaza a la planilla con las dos gráficas comparativas que hoy se
     * comparte como imagen.
     * </p>
     */
    @GetMapping("/trayectoria/{sondajeId}")
    public ResponseEntity<TrayectoriaSondajeDTO> trayectoria(@PathVariable UUID sondajeId) {
        log.info("HTTP GET - Trayectoria del sondaje: {}", sondajeId);
        return ResponseEntity.ok(desviacionService.trayectoria(sondajeId));
    }
}
