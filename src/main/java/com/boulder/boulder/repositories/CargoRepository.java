package com.boulder.boulder.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.boulder.boulder.entities.Cargo;

/**
 * Repositorio de acceso a datos para Cargo.
 */
@Repository
public interface CargoRepository extends JpaRepository<Cargo, UUID> {

    Optional<Cargo> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<Cargo> findByArea(String area);
}
