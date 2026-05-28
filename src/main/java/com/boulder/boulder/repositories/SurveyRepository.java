package com.boulder.boulder.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.boulder.boulder.entities.Survey;

@Repository
public interface SurveyRepository extends JpaRepository<Survey, UUID> {
    List<Survey> findBySondajeIdOrderByProfundidadAsc(UUID sondajeId);
}