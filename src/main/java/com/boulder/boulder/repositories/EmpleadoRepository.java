package com.boulder.boulder.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.boulder.boulder.entities.Empleado;

/**
 * Repositorio de acceso a datos para Empleado.
 */
@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado, UUID> {

    Optional<Empleado> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<Empleado> findByEstado(Boolean estado);

    List<Empleado> findByEmpresaId(UUID empresaId);

    List<Empleado> findByCargoId(UUID cargoId);

    /** Candidatos para un campo concreto: los perforistas de un parte de
     *  perforación, los geólogos de un parte de logueo. */
    List<Empleado> findByCargoAreaAndEstadoTrue(String area);
}
