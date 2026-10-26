package com.boulder.boulder.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.boulder.boulder.entities.UsuarioRol;

/**
 * Repositorio de acceso a datos para la asignación de roles a usuarios.
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface UsuarioRolRepository extends JpaRepository<UsuarioRol, UUID> {

    List<UsuarioRol> findByUsuarioId(UUID usuarioId);

    /** Usado al reemplazar el conjunto completo de roles de un usuario. */
    void deleteByUsuarioId(UUID usuarioId);

    /**
     * Cuántas cuentas <em>activas</em> tienen el rol indicado.
     *
     * <p>
     * Es la consulta que sostiene la salvaguarda contra el autobloqueo: si una
     * operación dejaría el sistema sin ningún administrador activo, no hay forma
     * de volver a entrar a administrar nada.
     * </p>
     */
    @Query("""
            SELECT COUNT(ur)
            FROM UsuarioRol ur
            WHERE ur.rol.codigo = :codigo
              AND ur.usuario.estado = TRUE
            """)
    long contarUsuariosActivosConRol(@Param("codigo") String codigo);
}
