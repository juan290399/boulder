package com.boulder.boulder.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.boulder.boulder.entities.Maquina;

@Repository
public interface MaquinaRepository extends JpaRepository<Maquina, UUID> {
    Optional<Maquina> findByCodigo(String codigo);
}