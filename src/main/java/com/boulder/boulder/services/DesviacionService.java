package com.boulder.boulder.services;

import java.util.List;

import com.boulder.boulder.dto.DesviacionMaquinaDTO;
import com.boulder.boulder.dto.DesviacionPerforistaDTO;
import com.boulder.boulder.dto.DesviacionSondajeDTO;
import com.boulder.boulder.dto.TrayectoriaSondajeDTO;

import java.util.UUID;

/**
 * Consultas de solo lectura para la analítica de desviaciones de sondaje,
 * respaldadas por las vistas {@code operacional.vw_desviacion_*}.
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
public interface DesviacionService {

    List<DesviacionPerforistaDTO> resumenPorPerforista();

    List<DesviacionMaquinaDTO> resumenPorMaquina();

    List<DesviacionSondajeDTO> evolucionPorSondaje(String sondajeCodigo);

    /**
     * Trayectoria planificada contra medida de un sondaje, para graficarla.
     *
     * @param sondajeId sondaje a consultar
     */
    TrayectoriaSondajeDTO trayectoria(UUID sondajeId);
}
