package com.boulder.boulder.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.boulder.boulder.entities.SurveyMedicion;

/**
 * Repositorio de acceso a datos para los eventos de medición de desviación.
 */
@Repository
public interface SurveyMedicionRepository extends JpaRepository<SurveyMedicion, UUID> {

    List<SurveyMedicion> findBySondajeIdOrderByProfundidadAscNroMedicionAsc(UUID sondajeId);

    List<SurveyMedicion> findByMaquinaId(UUID maquinaId);

    List<SurveyMedicion> findByPerforistaId(UUID perforistaId);
}
