package com.boulder.boulder.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.boulder.boulder.entities.Collar;

@Repository
public interface CollarRepository extends JpaRepository<Collar, UUID> {
    Optional<Collar> findByCodigo(String codigo);
}
