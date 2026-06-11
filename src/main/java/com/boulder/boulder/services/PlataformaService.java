package com.boulder.boulder.services;

import java.util.List;
import java.util.UUID;
import com.boulder.boulder.dto.PlataformaDTO;

/**
 * Define las operaciones de negocio relacionadas con la gestión de plataformas.
 *
 * <p>
 * Proporciona funcionalidades para administrar las plataformas físicas de perforación
 * asignadas a los frentes de trabajo.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
public interface PlataformaService {
    List<PlataformaDTO> listarTodas();
    PlataformaDTO obtenerPorId(UUID id);
    PlataformaDTO obtenerPorCodigo(String codigo);
    List<PlataformaDTO> listarPorProyecto(UUID proyectoId);
    PlataformaDTO guardar(PlataformaDTO dto);
    PlataformaDTO actualizar(UUID id, PlataformaDTO dto);
}