package com.boulder.boulder.services;

import java.util.List;
import java.util.UUID;

import com.boulder.boulder.dto.ProyectoDTO;

/**
 * Define las operaciones de negocio relacionadas con la gestión de proyectos.
 *
 * <p>
 * Proporciona funcionalidades para administrar los proyectos de exploración,
 * su información general y relaciones asociadas.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
public interface ProyectoService {
    List<ProyectoDTO> listarTodos();
    ProyectoDTO obtenerPorId(UUID id);
    ProyectoDTO obtenerPorCodigo(String codigo);
    ProyectoDTO guardar(ProyectoDTO dto);
    ProyectoDTO actualizar(UUID id, ProyectoDTO dto);
}
