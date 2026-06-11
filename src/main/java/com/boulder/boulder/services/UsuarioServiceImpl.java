package com.boulder.boulder.services;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boulder.boulder.dto.UsuarioDTO;
import com.boulder.boulder.entities.Usuario;
import com.boulder.boulder.entities.UsuarioRol;
import com.boulder.boulder.repositories.UsuarioRepository;
import com.boulder.boulder.repositories.UsuarioRolRepository;

import jakarta.persistence.EntityNotFoundException;
/**
 * Implementación de los servicios de negocio para la gestión de usuarios.
 *
 * <p>
 * Gestiona las operaciones relacionadas con el mantenimiento de usuarios y
 * el control de acceso al sistema.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Service
public class UsuarioServiceImpl implements UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioServiceImpl.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioRolRepository usuarioRolRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioDTO> listarTodos() {
        log.debug("Consultando el listado maestro de usuarios registrados en el sistema");
        return usuarioRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioDTO obtenerPorId(UUID id) {
        log.debug("Buscando usuario por ID único: {}", id);
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no localizado con el ID especificado."));
        return convertToDto(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioDTO obtenerPorUsuario(String username) {
        log.debug("Buscando credenciales operacionales para el usuario: {}", username);
        Usuario usuario = usuarioRepository.findByUsuario(username)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró ningún usuario con el identificador: " + username));
        return convertToDto(usuario);
    }

    @Override
    @Transactional
    public UsuarioDTO guardar(UsuarioDTO dto) {
        log.info("Iniciando registro de un nuevo usuario en la plataforma: {}", dto.getUsuario());
        try {
            if (usuarioRepository.existsByUsuario(dto.getUsuario())) {
                throw new IllegalArgumentException("El identificador de usuario '" + dto.getUsuario() + "' ya se encuentra registrado.");
            }

            Usuario usuario = new Usuario();
            usuario.setUsuario(dto.getUsuario());
            usuario.setNombres(dto.getNombres());    
            usuario.setApellidos(dto.getApellidos());  
            usuario.setCorreo(dto.getCorreo());
            usuario.setContrasenia(dto.getContrasenia());
            usuario.setEstado(dto.getEstado() != null ? dto.getEstado() : true);

            Usuario guardado = usuarioRepository.save(usuario);
            log.info("Usuario '{}' registrado exitosamente", guardado.getUsuario());
            return convertToDto(guardado);
        } catch (IllegalArgumentException e) {
            log.warn("Validación fallida al guardar usuario: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Fallo crítico al persistir el usuario: {}", dto.getUsuario(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public UsuarioDTO actualizar(UUID id, UsuarioDTO dto) {
        log.info("Actualizando información de perfil para el usuario ID: {}", id);
        try {
            Usuario usuario = usuarioRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("El usuario a modificar no existe en la base de datos."));

            usuario.setNombres(dto.getNombres());
            usuario.setApellidos(dto.getApellidos());
            usuario.setCorreo(dto.getCorreo());
            
            if (dto.getEstado() != null) {
                usuario.setEstado(dto.getEstado());
            }
            
            if (dto.getContrasenia() != null && !dto.getContrasenia().trim().isEmpty()) {
                usuario.setContrasenia(dto.getContrasenia());
            }

            Usuario actualizado = usuarioRepository.save(usuario);
            log.info("Perfil del usuario '{}' actualizado correctamente", actualizado.getUsuario());
            return convertToDto(actualizado);
        } catch (Exception e) {
            log.error("Error al modificar el registro del usuario con ID: {}", id, e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void cambiarEstado(UUID id, boolean estado) {
        log.warn("Cambiando estado de actividad para el usuario ID: {} a -> {}", id, estado);
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Registro de usuario no localizado para el cambio de estado."));
        
        usuario.setEstado(estado);
        usuarioRepository.save(usuario);
        log.info("Estado del usuario '{}' modificado exitosamente", usuario.getUsuario());
    }

    private UsuarioDTO convertToDto(Usuario usuario) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(usuario.getId());
        dto.setNumeroInterno(usuario.getNumeroInterno());
        dto.setUsuario(usuario.getUsuario());
        dto.setNombres(usuario.getNombres());       
        dto.setApellidos(usuario.getApellidos());   
        dto.setCorreo(usuario.getCorreo());
        dto.setEstado(usuario.getEstado());         
        
        if (usuario.getId() != null) {
            List<UsuarioRol> rolesUsuario = usuarioRolRepository.findByUsuarioId(usuario.getId());
            
            List<String> listaRoles = rolesUsuario.stream()
                    .map(ur -> ur.getRol().getCodigo())
                    .collect(Collectors.toList());
                    
            dto.setRoles(listaRoles);
        }

        return dto;
    }
}