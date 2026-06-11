package com.boulder.boulder.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.boulder.boulder.entities.Sondaje;

/**
 * Repositorio de acceso a datos para Sondaje.
 *
 * <p>
 * Gestiona información de perforaciones ejecutadas en campo.
 * </p>
 */
@Repository
public interface SondajeRepository extends JpaRepository<Sondaje, UUID> {

    List<Sondaje> findByProyectoId(UUID proyectoId);
    List<Sondaje> findByProgramaSondajeId(UUID programaSondajeId);
    Optional<Sondaje> findByCollarId(UUID collarId);
    List<Sondaje> findByPlataformaId(UUID plataformaId);
    List<Sondaje> findByMaquinaId(UUID maquinaId);
    Optional<Sondaje> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
}