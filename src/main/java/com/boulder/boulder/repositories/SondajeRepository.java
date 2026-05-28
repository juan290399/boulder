package com.boulder.boulder.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.boulder.boulder.entities.Sondaje;

@Repository
public interface SondajeRepository extends JpaRepository<Sondaje, UUID> {
    Optional<Sondaje> findByCodigo(String codigo);
    
    // Buscar los pozos reales que se desprenden de un programa de planificación
    List<Sondaje> findByProgramaSondajeId(UUID programaSondajeId);
    
    List<Sondaje> findByProyectoId(UUID proyectoId);
}