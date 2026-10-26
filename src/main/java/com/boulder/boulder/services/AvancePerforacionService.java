package com.boulder.boulder.services;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.boulder.boulder.dto.AvancePerforacionDTO;

/**
 * Operaciones de negocio para los partes diarios de avance de perforación (UC15).
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
public interface AvancePerforacionService {
    List<AvancePerforacionDTO> listarTodos();
    AvancePerforacionDTO obtenerPorId(UUID id);
    List<AvancePerforacionDTO> listarPorSondaje(UUID sondajeId);
    List<AvancePerforacionDTO> listarPorFecha(LocalDate fecha);
    AvancePerforacionDTO guardar(AvancePerforacionDTO dto);
    AvancePerforacionDTO actualizar(UUID id, AvancePerforacionDTO dto);
    void eliminar(UUID id);
}
