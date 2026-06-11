package com.boulder.boulder.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.boulder.boulder.entities.Rol;

/**
 * Repositorio de acceso a datos para Rol.
 */
@Repository
public interface RolRepository extends JpaRepository<Rol, UUID> {

    Optional<Rol> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);
}