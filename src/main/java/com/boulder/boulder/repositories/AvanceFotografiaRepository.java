package com.boulder.boulder.repositories;

import org.springframework.stereotype.Repository;

import com.boulder.boulder.entities.AvanceFotografia;

/**
 * Repositorio de acceso a datos para los partes de registro fotográfico (UC17).
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface AvanceFotografiaRepository extends AvanceOperativoRepository<AvanceFotografia> {
}
