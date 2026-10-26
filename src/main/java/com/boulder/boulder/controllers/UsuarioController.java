package com.boulder.boulder.controllers;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boulder.boulder.dto.UsuarioDTO;
import com.boulder.boulder.services.UsuarioService;

/**
 * Controlador REST para la administración de usuarios, perfiles y estados de cuenta.
 *
 * <p>
 * Todo el controlador es exclusivo del rol ADMIN. La regla vive además en
 * {@code SecurityConfig} para {@code /api/base/**}; la anotación la repite aquí
 * para que la intención quede escrita junto al código que protege y siga en pie
 * si algún día cambia la configuración de rutas.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/base/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {

    private static final Logger log = LoggerFactory.getLogger(UsuarioController.class);

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> listarTodos() {
        log.info("HTTP GET - Solicitud de listado de usuarios del sistema");
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> obtenerPorId(@PathVariable UUID id) {
        log.info("HTTP GET - Buscando cuenta de usuario ID: {}", id);
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UsuarioDTO> obtenerPorUsuario(@PathVariable String username) {
        log.info("HTTP GET - Consultando perfil operacional del identificador: {}", username);
        return ResponseEntity.ok(usuarioService.obtenerPorUsuario(username));
    }

    @PostMapping
    public ResponseEntity<UsuarioDTO> guardar(@RequestBody UsuarioDTO dto) {
        log.info("HTTP POST - Solicitud de alta para el nuevo usuario: {}", dto.getUsuario());
        UsuarioDTO guardado = usuarioService.guardar(dto);
        return new ResponseEntity<>(guardado, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDTO> actualizar(@PathVariable UUID id, @RequestBody UsuarioDTO dto) {
        log.info("HTTP PUT - Modificando ficha de datos personales del usuario ID: {}", id);
        return ResponseEntity.ok(usuarioService.actualizar(id, dto));
    }

    /**
     * Reemplaza el conjunto completo de roles del usuario.
     *
     * <p>
     * El cuerpo es la lista de códigos tal como están en {@code base.rol.codigo}
     * ({@code ["ADMIN","GEOLOGO"]}), sin el prefijo {@code ROLE_}. Una lista vacía
     * revoca todos los roles.
     * </p>
     *
     * <p>
     * Los cambios no alcanzan a los tokens ya emitidos: el usuario afectado
     * conserva sus permisos anteriores hasta que vuelve a iniciar sesión.
     * </p>
     */
    @PutMapping("/{id}/roles")
    public ResponseEntity<UsuarioDTO> asignarRoles(@PathVariable UUID id, @RequestBody List<String> codigos) {
        log.warn("HTTP PUT - Reasignando roles del usuario ID: {} -> {}", id, codigos);
        return ResponseEntity.ok(usuarioService.asignarRoles(id, codigos));
    }

    /**
     * Endpoint atómico para activar o suspender una cuenta de usuario de forma rápida.
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstado(@PathVariable UUID id, @RequestParam boolean activo) {
        log.warn("HTTP PATCH - Modificando disponibilidad de acceso para el usuario ID: {} -> activo: {}", id, activo);
        usuarioService.cambiarEstado(id, activo);
        return ResponseEntity.noContent().build();
    }
}
