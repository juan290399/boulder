package com.boulder.boulder.services;

import java.util.List;
import java.util.UUID;

import com.boulder.boulder.dto.MaquinaDTO;

/**
 * Define las operaciones de negocio relacionadas con la gestión de máquinas.
 *
 * <p>
 * Proporciona funcionalidades para administrar las máquinas de perforación y
 * su información operativa.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
public interface MaquinaService {
    List<MaquinaDTO> listarTodas();
    MaquinaDTO obtenerPorId(UUID id);
    MaquinaDTO obtenerPorCodigo(String codigo);
    MaquinaDTO guardar(MaquinaDTO dto);
    MaquinaDTO actualizar(UUID id, MaquinaDTO dto);
}
