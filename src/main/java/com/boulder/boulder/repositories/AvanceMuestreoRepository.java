package com.boulder.boulder.repositories;

import org.springframework.stereotype.Repository;

import com.boulder.boulder.entities.AvanceMuestreo;

/**
 * Repositorio de acceso a datos para los partes de muestreo geoquímico (CU17).
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface AvanceMuestreoRepository extends AvanceOperativoRepository<AvanceMuestreo> {
}
