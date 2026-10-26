package com.boulder.boulder.controllers;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.boulder.boulder.dto.UsuarioDTO;
import com.boulder.boulder.dto.cuenta.CambioContrasenaDTO;
import com.boulder.boulder.services.CambioContrasenaService;
import com.boulder.boulder.services.UsuarioService;

import jakarta.validation.Valid;

/**
 * La cuenta del usuario que inició sesión (pantalla P14, «Mi cuenta»).
 *
 * <p>Todas las operaciones toman el usuario del token ({@link Principal}), nunca
 * de la URL ni del cuerpo: cualquier usuario autenticado puede ver y cambiar
 * solo lo suyo, sin necesidad del rol ADMIN.</p>
 */
@RestController
@RequestMapping("/api/cuenta")
public class CuentaController {

    private final UsuarioService usuarios;
    private final CambioContrasenaService cambioContrasena;

    public CuentaController(UsuarioService usuarios, CambioContrasenaService cambioContrasena) {
        this.usuarios = usuarios;
        this.cambioContrasena = cambioContrasena;
    }

    /** Datos de la cuenta propia. La contraseña no viaja: el DTO la marca solo de escritura. */
    @GetMapping
    public UsuarioDTO miCuenta(Principal principal) {
        return usuarios.obtenerPorUsuario(principal.getName());
    }

    /**
     * Cambia la contraseña propia (RF02).
     *
     * @return 204 si se cambió; 400 con el motivo si la actual no coincide o la nueva no cumple las reglas
     */
    @PutMapping("/contrasena")
    public ResponseEntity<Void> cambiarContrasena(Principal principal, @Valid @RequestBody CambioContrasenaDTO dto) {
        cambioContrasena.cambiar(principal.getName(), dto.actual(), dto.nueva());
        return ResponseEntity.noContent().build();
    }
}
