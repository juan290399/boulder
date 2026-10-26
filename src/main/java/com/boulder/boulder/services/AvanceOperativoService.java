package com.boulder.boulder.services;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.boulder.boulder.dto.AvanceOperativoDTO;

/**
 * Contrato común de los servicios de partes de avance operativo (UC16 a UC20).
 *
 * <p>
 * Los siete partes se manipulan igual —alta, edición, baja y consulta por
 * sondaje o por fecha—, así que el contrato se declara una sola vez y cada
 * servicio concreto lo cierra sobre su propio DTO.
 * </p>
 *
 * @param <D> tipo de DTO que maneja el servicio
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
public interface AvanceOperativoService<D extends AvanceOperativoDTO> {

    List<D> listarTodos();

    D obtenerPorId(UUID id);

    List<D> listarPorSondaje(UUID sondajeId);

    List<D> listarPorFecha(LocalDate fecha);

    List<D> listarPorRango(LocalDate desde, LocalDate hasta);

    D guardar(D dto);

    D actualizar(UUID id, D dto);

    void eliminar(UUID id);
}
