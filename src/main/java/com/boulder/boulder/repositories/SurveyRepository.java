package com.boulder.boulder.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.boulder.boulder.entities.Survey;

/**
 * Repositorio de acceso a datos para Survey.
 *
 * <p>
 * Maneja las mediciones de trayectoria de los sondajes.
 * </p>
 */
@Repository
public interface SurveyRepository extends JpaRepository<Survey, UUID> {

    List<Survey> findBySondajeId(UUID sondajeId);

    List<Survey> findBySondajeIdOrderByProfundidadAsc(UUID sondajeId);
}