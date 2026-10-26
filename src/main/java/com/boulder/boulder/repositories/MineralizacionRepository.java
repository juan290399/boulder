package com.boulder.boulder.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.boulder.boulder.entities.Mineralizacion;

/**
 * Repositorio de acceso a datos para los tramos de mineralización.
 *
 * <p>Se consulta siempre por sondaje y nunca entero: la campaña acumula decenas
 * de miles de filas y quien las pide es un tablet que solo loguea un pozo.</p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface MineralizacionRepository extends JpaRepository<Mineralizacion, UUID> {

    /** En orden de profundidad, que es como se lee un log. */
    List<Mineralizacion> findBySondajeIdOrderByDesdeAsc(UUID sondajeId);

    long countBySondajeId(UUID sondajeId);
}
