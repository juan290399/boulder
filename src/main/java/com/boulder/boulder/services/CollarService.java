package com.boulder.boulder.services;

import java.util.List;
import java.util.UUID;
import com.boulder.boulder.dto.CollarDTO;

/**
 * Define las operaciones de negocio relacionadas con el control de bocas de pozo (Collar).
 *
 * <p>
 * Proporciona funcionalidades para registrar, auditar y actualizar las coordenadas reales
 * y parámetros técnicos medidos en el punto de inicio de la perforación.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
public interface CollarService {
    List<CollarDTO> listarTodos();
    CollarDTO obtenerPorId(UUID id);
    CollarDTO obtenerPorCodigo(String codigo);
    CollarDTO guardar(CollarDTO dto);
    CollarDTO actualizar(UUID id, CollarDTO dto);
}