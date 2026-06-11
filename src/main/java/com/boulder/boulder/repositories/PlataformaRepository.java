package com.boulder.boulder.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.boulder.boulder.entities.Plataforma;

/**
 * Repositorio de acceso a datos para Plataforma.
 */
@Repository
public interface PlataformaRepository extends JpaRepository<Plataforma, UUID> {

    Optional<Plataforma> findByCodigo(String codigo);
    List<Plataforma> findByProyectoId(UUID proyectoId);
    boolean existsByCodigo(String codigo);
}