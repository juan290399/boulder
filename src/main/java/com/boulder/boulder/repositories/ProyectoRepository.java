package com.boulder.boulder.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.boulder.boulder.entities.Proyecto;

/**
 * Repositorio de acceso a datos para Proyecto.
 */
@Repository
public interface ProyectoRepository extends JpaRepository<Proyecto, UUID> {

    Optional<Proyecto> findByCodigo(String codigo);
    List<Proyecto> findByEmpresaId(UUID empresaId);
    List<Proyecto> findBySrid(Integer srid);
    boolean existsByCodigo(String codigo);
}