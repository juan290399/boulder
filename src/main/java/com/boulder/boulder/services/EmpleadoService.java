package com.boulder.boulder.services;

import java.util.List;
import java.util.UUID;

import com.boulder.boulder.dto.CargoDTO;
import com.boulder.boulder.dto.EmpleadoDTO;

/**
 * Operaciones de negocio para el catálogo de personal.
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
public interface EmpleadoService {

    List<EmpleadoDTO> listarTodos();

    /** Solo activos, filtrando por área del cargo ({@code perforacion},
     *  {@code geologia}...). Es lo que alimenta los desplegables de los partes. */
    List<EmpleadoDTO> listarPorArea(String area);

    EmpleadoDTO obtenerPorId(UUID id);

    EmpleadoDTO obtenerPorCodigo(String codigo);

    EmpleadoDTO guardar(EmpleadoDTO dto);

    EmpleadoDTO actualizar(UUID id, EmpleadoDTO dto);

    /**
     * Da de baja al empleado.
     *
     * <p>
     * No hay borrado: los partes de perforación y las mediciones apuntan a
     * estas filas, y eliminarlas dejaría los registros históricos sin autor.
     * </p>
     */
    void cambiarEstado(UUID id, boolean activo);

    List<CargoDTO> listarCargos();
}
