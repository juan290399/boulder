package com.boulder.boulder.services;

import java.util.List;
import java.util.UUID;
import com.boulder.boulder.dto.ProyectoMaquinaDTO;

/**
 * Define las operaciones de negocio relacionadas con la asignación logistica de maquinaria a proyectos.
 *
 * <p>
 * Proporciona las funcionalidades para registrar reubicaciones de equipos, liberar maquinaria
 * y auditar el historial de flota en terreno.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
public interface ProyectoMaquinaService {
    List<ProyectoMaquinaDTO> listarTodas();
    ProyectoMaquinaDTO obtenerPorId(UUID id);
    List<ProyectoMaquinaDTO> listarPorProyecto(UUID proyectoId);
    ProyectoMaquinaDTO guardar(ProyectoMaquinaDTO dto);
    ProyectoMaquinaDTO actualizar(UUID id, ProyectoMaquinaDTO dto);
}