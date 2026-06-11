package com.boulder.boulder.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.boulder.boulder.entities.Collar;

/**
 * Repositorio de acceso a datos para Collar.
 */
@Repository
public interface CollarRepository extends JpaRepository<Collar, UUID> {

    Optional<Collar> findByCodigo(String codigo);
    List<Collar> findByCampania(String campania);
    boolean existsByCodigo(String codigo);
}