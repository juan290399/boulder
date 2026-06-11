package com.boulder.boulder.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.boulder.boulder.entities.ProyectoMaquina;

/**
 * Repositorio para la gestión de persistencia de la asignación de maquinarias a proyectos.
 * Al haber migrado a un ID subrogado, hereda las operaciones CRUD estándar usando UUID.
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface ProyectoMaquinaRepository extends JpaRepository<ProyectoMaquina, UUID> {

    List<ProyectoMaquina> findByProyectoId(UUID proyectoId);
}