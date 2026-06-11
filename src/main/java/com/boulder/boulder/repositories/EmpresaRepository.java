package com.boulder.boulder.repositories;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.boulder.boulder.entities.Empresa;

/**
 * Repositorio para la entidad Empresa.
 *
 * <p>
 * Permite realizar operaciones CRUD y consultas sobre las empresas registradas
 * en el sistema.
 * </p>
 */
@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, UUID> {

    Optional<Empresa> findByRuc(String ruc);
    boolean existsByCodigo(String codigo);
}