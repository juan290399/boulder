package com.boulder.boulder.services;

import java.util.List;
import java.util.UUID;
import com.boulder.boulder.dto.ProgramaSondajeDTO;

/**
 * Define las operaciones de negocio relacionadas con la planificación de campañas de sondaje.
 *
 * <p>
 * Proporciona funciones para administrar los metros proyectados, objetivos de perforación
 * y su avance cronológico por proyecto.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
public interface ProgramaSondajeService {
    List<ProgramaSondajeDTO> listarTodos();
    ProgramaSondajeDTO obtenerPorId(UUID id);
    ProgramaSondajeDTO obtenerPorCodigo(String codigo);
    List<ProgramaSondajeDTO> listarPorProyecto(UUID proyectoId);
    List<ProgramaSondajeDTO> listarPorPlataforma(UUID plataformaId);
    ProgramaSondajeDTO guardar(ProgramaSondajeDTO dto);
    ProgramaSondajeDTO actualizar(UUID id, ProgramaSondajeDTO dto);
}