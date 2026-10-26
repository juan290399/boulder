package com.boulder.boulder.repositories;

import org.springframework.stereotype.Repository;

import com.boulder.boulder.entities.RecepcionCaja;

/**
 * Repositorio de acceso a datos para los partes de recepción de cajas de campo (CU12).
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface RecepcionCajaRepository extends AvanceOperativoRepository<RecepcionCaja> {
}
