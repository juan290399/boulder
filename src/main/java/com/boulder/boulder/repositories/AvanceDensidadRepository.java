package com.boulder.boulder.repositories;

import org.springframework.stereotype.Repository;

import com.boulder.boulder.entities.AvanceDensidad;

/**
 * Repositorio de acceso a datos para los partes de estufa y cálculo de densidad (CU16).
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface AvanceDensidadRepository extends AvanceOperativoRepository<AvanceDensidad> {
}
