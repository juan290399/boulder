package com.boulder.boulder.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.boulder.boulder.entities.ProgramaSondaje;

@Repository
public interface ProgramaSondajeRepository extends JpaRepository<ProgramaSondaje, UUID> {
    Optional<ProgramaSondaje> findByCodigo(String codigo);
    
    // Listar planes/programas asociados a un proyecto
    List<ProgramaSondaje> findByProyectoId(UUID proyectoId);
}