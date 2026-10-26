package com.boulder.boulder.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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

    /** Los partes de un sondaje en un día, para detectar un turno cargado dos veces. */
    List<AvancePerforacion> findBySondajeIdAndFecha(UUID sondajeId, LocalDate fecha);

    /** El parte que llegó más hondo: de ahí arranca el turno siguiente. */
    Optional<AvancePerforacion> findFirstBySondajeIdOrderByHastaDescFechaDesc(UUID sondajeId);

    List<AvancePerforacion> findByMaquinaId(UUID maquinaId);
}
