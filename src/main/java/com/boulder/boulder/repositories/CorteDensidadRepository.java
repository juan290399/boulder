package com.boulder.boulder.repositories;

import org.springframework.stereotype.Repository;

import com.boulder.boulder.entities.CorteDensidad;

/**
 * Repositorio de acceso a datos para los partes de corte de densidad (CU15).
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface CorteDensidadRepository extends AvanceOperativoRepository<CorteDensidad> {
}
