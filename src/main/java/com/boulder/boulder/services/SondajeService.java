package com.boulder.boulder.services;

import java.util.List;
import java.util.UUID;

import com.boulder.boulder.dto.SondajeDTO;

public interface SondajeService {
    List<SondajeDTO> listarPorProyecto(UUID proyectoId);
    SondajeDTO obtenerPorCodigo(String codigo);
    SondajeDTO guardar(SondajeDTO dto);
    SondajeDTO actualizarAvance(UUID id, Double nuevaProfundidad);
}