package com.boulder.boulder.services;

import java.util.List;
import java.util.UUID;
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
    UsuarioDTO actualizar(UUID id, UsuarioDTO dto);
    void cambiarEstado(UUID id, boolean activo);
}