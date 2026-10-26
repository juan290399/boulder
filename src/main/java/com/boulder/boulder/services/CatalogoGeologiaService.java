package com.boulder.boulder.services;

import java.util.List;

import com.boulder.boulder.dto.CatalogoGeologiaDTO;

/**
 * Entrega los diccionarios del esquema {@code geologia}.
 *
 * <p>Son datos de solo lectura para quien los consume: los mantiene el equipo de
 * geología desde la plataforma web, y ni el tablet ni esta capa los modifican.</p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
public interface CatalogoGeologiaService {

    /** Los veintiún catálogos aplanados en una sola lista con discriminador. */
    List<CatalogoGeologiaDTO> listarTodos();
}
