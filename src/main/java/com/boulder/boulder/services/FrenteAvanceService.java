package com.boulder.boulder.services;

import java.util.List;

import com.boulder.boulder.dto.FrenteAvanceDTO;

/**
 * Servicios de consulta del frente de avance por sondaje.
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
public interface FrenteAvanceService {

    /**
     * Devuelve el estado de cada sondaje a lo largo del flujo de testigo.
     *
     * @param soloActivos si es {@code true}, deja fuera los sondajes cerrados
     */
    List<FrenteAvanceDTO> consultar(boolean soloActivos);
}
