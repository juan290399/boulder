package com.boulder.boulder.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.boulder.boulder.entities.ProgramaSondaje;

/**
 * Repositorio de acceso a datos para ProgramaSondaje.
 *
 * <p>
 * Maneja consultas relacionadas con la planificación de sondajes dentro de un proyecto.
 * </p>
 */
@Repository
public interface ProgramaSondajeRepository extends JpaRepository<ProgramaSondaje, UUID> {

    Optional<ProgramaSondaje> findByCodigo(String codigo);
    List<ProgramaSondaje> findByProyectoId(UUID proyectoId);
    List<ProgramaSondaje> findByPlataformaId(UUID plataformaId);
    List<ProgramaSondaje> findByPrioridad(String prioridad);
    boolean existsByCodigo(String codigo);
}