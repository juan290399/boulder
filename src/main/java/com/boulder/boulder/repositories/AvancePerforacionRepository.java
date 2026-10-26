package com.boulder.boulder.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.boulder.boulder.entities.AvancePerforacion;

/**
 * Repositorio de acceso a datos para los partes diarios de avance de perforación.
 */
@Repository
public interface AvancePerforacionRepository extends JpaRepository<AvancePerforacion, UUID> {

    List<AvancePerforacion> findBySondajeIdOrderByFechaAscTurnoAsc(UUID sondajeId);

    List<AvancePerforacion> findByFecha(LocalDate fecha);

    List<AvancePerforacion> findByMaquinaId(UUID maquinaId);
}
