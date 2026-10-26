package com.boulder.boulder.services;

import java.util.List;
import java.util.UUID;
import com.boulder.boulder.dto.RegistroDTO;
import com.boulder.boulder.dto.UsuarioDTO;
/**
 * Define las operaciones de negocio relacionadas con la gestión de usuarios.
 *
 * <p>
 * Proporciona funcionalidades para administrar la información de usuarios,
 * autenticación y asignación de roles.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
public interface UsuarioService {
    List<UsuarioDTO> listarTodos();
    UsuarioDTO obtenerPorId(UUID id);
    UsuarioDTO obtenerPorUsuario(String usuario);
    UsuarioDTO guardar(UsuarioDTO dto);

    /**
     * Auto-registro público. Crea la cuenta siempre inactiva y sin roles;
     * un administrador debe habilitarla y asignarle permisos.
     */
    UsuarioDTO registrar(RegistroDTO dto);

    UsuarioDTO actualizar(UUID id, UsuarioDTO dto);
    void cambiarEstado(UUID id, boolean activo);

    /**
     * Reemplaza el conjunto completo de roles del usuario por el indicado.
     *
     * <p>
     * Es una operación de reemplazo, no incremental: los roles que no vengan en
     * la lista quedan revocados. Resulta idempotente, que es lo que conviene
     * cuando el origen es un formulario de casillas de verificación.
     * </p>
     *
     * @param id      identificador del usuario
     * @param codigos códigos de rol tal como figuran en {@code base.rol.codigo}
     *                (ADMIN, GEOLOGO, ...), sin el prefijo {@code ROLE_}
     */
    UsuarioDTO asignarRoles(UUID id, List<String> codigos);
}
