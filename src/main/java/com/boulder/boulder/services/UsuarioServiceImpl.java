package com.boulder.boulder.services;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boulder.boulder.dto.RegistroDTO;
import com.boulder.boulder.dto.UsuarioDTO;
import com.boulder.boulder.entities.Empleado;
import com.boulder.boulder.entities.Rol;
import com.boulder.boulder.entities.Usuario;
import com.boulder.boulder.entities.UsuarioRol;
import com.boulder.boulder.repositories.EmpleadoRepository;
import com.boulder.boulder.repositories.RolRepository;
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

    /** Código del rol administrador en {@code base.rol.codigo}. */
    private static final String ROL_ADMIN = "ADMIN";

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioRolRepository usuarioRolRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    /** La contraseña nunca se guarda tal como llega: siempre pasa por aquí. */
    @Autowired
    private PasswordEncoder passwordEncoder;

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
            if (usuarioRepository.existsByCorreo(dto.getCorreo())) {
                throw new IllegalArgumentException("El correo '" + dto.getCorreo() + "' ya se encuentra registrado.");
            }

            Usuario usuario = new Usuario();
            usuario.setUsuario(dto.getUsuario());
            usuario.setNombres(dto.getNombres());
            usuario.setApellidos(dto.getApellidos());
            usuario.setCorreo(dto.getCorreo());
            usuario.setContrasenia(passwordEncoder.encode(dto.getContrasenia()));
            usuario.setEstado(dto.getEstado() != null ? dto.getEstado() : true);
            aplicarEmpleado(dto, usuario);

            Usuario guardado = usuarioRepository.save(usuario);

            // A diferencia del auto-registro, un alta hecha por un administrador
            // puede nacer ya con roles y activa: es el camino previsto para dar
            // de alta al personal que no se registra solo.
            if (dto.getRoles() != null && !dto.getRoles().isEmpty()) {
                reemplazarRoles(guardado, dto.getRoles());
            }

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
    public UsuarioDTO registrar(RegistroDTO dto) {
        log.info("Auto-registro solicitado para el usuario: {}", dto.getUsuario());

        if (usuarioRepository.existsByUsuario(dto.getUsuario())) {
            throw new IllegalArgumentException(
                    "El identificador de usuario '" + dto.getUsuario() + "' ya se encuentra registrado.");
        }
        if (usuarioRepository.existsByCorreo(dto.getCorreo())) {
            throw new IllegalArgumentException(
                    "El correo '" + dto.getCorreo() + "' ya se encuentra registrado.");
        }

        Usuario usuario = new Usuario();
        usuario.setUsuario(dto.getUsuario());
        usuario.setNombres(dto.getNombres());
        usuario.setApellidos(dto.getApellidos());
        usuario.setCorreo(dto.getCorreo());
        usuario.setContrasenia(passwordEncoder.encode(dto.getContrasenia()));

        // La cuenta nace inactiva y sin roles: un JWT sin authorities pasaría el
        // filtro `authenticated()`, así que la activación queda en manos de un
        // administrador (PATCH /api/base/usuarios/{id}/estado).
        usuario.setEstado(false);

        Usuario guardado = usuarioRepository.save(usuario);
        log.info("Usuario '{}' auto-registrado; pendiente de activación por un administrador",
                guardado.getUsuario());
        return convertToDto(guardado);
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
                verificarDesactivacionPropia(usuario, dto.getEstado());
                usuario.setEstado(dto.getEstado());
            }

            if (dto.getContrasenia() != null && !dto.getContrasenia().trim().isEmpty()) {
                usuario.setContrasenia(passwordEncoder.encode(dto.getContrasenia()));
            }

            aplicarEmpleado(dto, usuario);

            Usuario actualizado = usuarioRepository.saveAndFlush(usuario);

            // `actualizar` deliberadamente NO toca los roles: se gestionan por
            // PUT /api/base/usuarios/{id}/roles, para que un formulario de datos
            // personales que no incluya el campo no los borre sin querer.
            verificarQuedaAlgunAdmin();

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

        verificarDesactivacionPropia(usuario, estado);

        usuario.setEstado(estado);
        usuarioRepository.saveAndFlush(usuario);

        verificarQuedaAlgunAdmin();

        log.info("Estado del usuario '{}' modificado exitosamente", usuario.getUsuario());
    }

    @Override
    @Transactional
    public UsuarioDTO asignarRoles(UUID id, List<String> codigos) {
        log.warn("Reemplazando el conjunto de roles del usuario ID: {} por -> {}", id, codigos);

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("El usuario a modificar no existe en la base de datos."));

        List<String> solicitados = codigos != null ? codigos : List.of();

        // Quitarse a uno mismo el rol ADMIN deja la sesión en curso con un token
        // que ya no vale para nada: la siguiente petición al panel responde 403 y
        // la única salida es que otro administrador lo devuelva.
        if (esUsuarioActual(usuario) && tieneRol(usuario, ROL_ADMIN) && !contieneAdmin(solicitados)) {
            throw new IllegalStateException(
                    "No puedes quitarte a ti mismo el rol ADMIN. Pídeselo a otro administrador.");
        }

        reemplazarRoles(usuario, solicitados);

        verificarQuedaAlgunAdmin();

        log.info("Roles del usuario '{}' actualizados a {}", usuario.getUsuario(), solicitados);
        return convertToDto(usuario);
    }

    /**
     * Borra las asignaciones actuales y escribe las indicadas.
     *
     * <p>
     * El {@code flush} intermedio no es opcional: sin él las bajas y las altas se
     * envían juntas al final de la transacción y la restricción
     * {@code usuario_rol_uq} salta al reasignar un rol que el usuario ya tenía.
     * </p>
     */
    private void reemplazarRoles(Usuario usuario, List<String> codigos) {
        usuarioRolRepository.deleteByUsuarioId(usuario.getId());
        usuarioRolRepository.flush();

        // LinkedHashSet: tolera códigos repetidos en la petición sin violar la
        // restricción de unicidad, y conserva el orden para que el log se lea igual.
        Set<String> unicos = new LinkedHashSet<>(codigos);

        List<UsuarioRol> nuevas = new ArrayList<>();
        for (String codigo : unicos) {
            Rol rol = rolRepository.findByCodigo(codigo)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "El rol '" + codigo + "' no existe en el catálogo."));

            UsuarioRol asignacion = new UsuarioRol();
            asignacion.setUsuario(usuario);
            asignacion.setRol(rol);
            nuevas.add(asignacion);
        }

        usuarioRolRepository.saveAllAndFlush(nuevas);
    }

    /**
     * Invariante del sistema: siempre debe quedar al menos un administrador
     * activo. Se comprueba después de aplicar el cambio y con la sesión ya
     * volcada, de modo que al lanzar la excepción la transacción revierte.
     */
    private void verificarQuedaAlgunAdmin() {
        if (usuarioRolRepository.contarUsuariosActivosConRol(ROL_ADMIN) == 0) {
            throw new IllegalStateException(
                    "La operación dejaría al sistema sin ningún administrador activo.");
        }
    }

    private void verificarDesactivacionPropia(Usuario usuario, boolean estadoNuevo) {
        if (!estadoNuevo && esUsuarioActual(usuario)) {
            throw new IllegalStateException("No puedes desactivar tu propia cuenta.");
        }
    }

    private boolean esUsuarioActual(Usuario usuario) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return false;
        }
        return auth.getName().equals(usuario.getUsuario());
    }

    private boolean tieneRol(Usuario usuario, String codigo) {
        return usuarioRolRepository.findByUsuarioId(usuario.getId()).stream()
                .anyMatch(ur -> codigo.equals(ur.getRol().getCodigo()));
    }

    /**
     * Enlaza o desenlaza la cuenta con un empleado del catálogo.
     *
     * <p>
     * La restricción UNIQUE de la columna impide que dos cuentas apunten al
     * mismo empleado; el rechazo llega como violación de integridad (409).
     * </p>
     */
    private void aplicarEmpleado(UsuarioDTO dto, Usuario usuario) {
        if (dto.getEmpleadoId() == null) {
            usuario.setEmpleado(null);
            return;
        }
        Empleado empleado = empleadoRepository.findById(dto.getEmpleadoId())
                .orElseThrow(() -> new EntityNotFoundException("El empleado asignado no existe."));
        usuario.setEmpleado(empleado);
    }

    private boolean contieneAdmin(List<String> codigos) {
        return codigos.stream().anyMatch(ROL_ADMIN::equals);
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

        if (usuario.getEmpleado() != null) {
            dto.setEmpleadoId(usuario.getEmpleado().getId());
            dto.setEmpleadoNombre((usuario.getEmpleado().getNombres() + " "
                    + (usuario.getEmpleado().getApellidos() != null
                            ? usuario.getEmpleado().getApellidos() : "")).trim());
        }

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
