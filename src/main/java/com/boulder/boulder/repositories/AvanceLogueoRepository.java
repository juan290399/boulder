package com.boulder.boulder.repositories;

import org.springframework.stereotype.Repository;

import com.boulder.boulder.entities.AvanceLogueo;

/**
 * Repositorio de acceso a datos para los partes diarios de avance de logueo (CU18).
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface AvanceLogueoRepository extends AvanceOperativoRepository<AvanceLogueo> {
}
