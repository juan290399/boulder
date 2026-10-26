package com.boulder.boulder.repositories;

import org.springframework.stereotype.Repository;

import com.boulder.boulder.entities.CorteMuestra;

/**
 * Repositorio de acceso a datos para los partes de corte geoquímico (UC18).
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface CorteMuestraRepository extends AvanceOperativoRepository<CorteMuestra> {
}
