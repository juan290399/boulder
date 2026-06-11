package com.boulder.boulder.services;
import java.util.List;
import java.util.UUID;
import com.boulder.boulder.dto.EmpresaDTO;
/**
 * Define las operaciones de negocio relacionadas con la gestión de empresas.
 *
 * <p>
 * Proporciona funcionalidades para consultar, registrar y actualizar la
 * información de las empresas registradas en el sistema.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
public interface EmpresaService {
    List<EmpresaDTO> listarTodas();
    EmpresaDTO obtenerPorId(UUID id);
    EmpresaDTO obtenerPorRuc(String ruc);
    EmpresaDTO guardar(EmpresaDTO dto);
    EmpresaDTO actualizar(UUID id, EmpresaDTO dto);
}
