package com.boulder.boulder.services;

import java.util.List;
import java.util.UUID;

import com.boulder.boulder.dto.MaquinaDTO;

public interface MaquinaService {
    List<MaquinaDTO> listarTodas();
    MaquinaDTO obtenerPorId(UUID id);
    MaquinaDTO obtenerPorCodigo(String codigo);
    MaquinaDTO guardar(MaquinaDTO dto);
    MaquinaDTO actualizar(UUID id, MaquinaDTO dto);
}
