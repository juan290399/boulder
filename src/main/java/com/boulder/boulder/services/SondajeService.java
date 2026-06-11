package com.boulder.boulder.services;

import java.util.List;
import java.util.UUID;
import com.boulder.boulder.dto.SondajeDTO;

/**
 * Define las operaciones de negocio relacionadas con el registro operativo de sondajes.
 *
 * <p>
 * Proporciona funcionalidades para listar, registrar y actualizar la ejecución física
 * de las perforaciones en terreno.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
public interface SondajeService {
    List<SondajeDTO> listarTodos();
    SondajeDTO obtenerPorId(UUID id);
    SondajeDTO obtenerPorCodigo(String codigo);
    SondajeDTO obtenerPorCollar(UUID collarId);
    List<SondajeDTO> listarPorProyecto(UUID proyectoId);
    List<SondajeDTO> listarPorMaquina(UUID maquinaId);
    List<SondajeDTO> listarPorPlataforma(UUID plataformaId);
    SondajeDTO guardar(SondajeDTO dto);
    SondajeDTO actualizar(UUID id, SondajeDTO dto);
}