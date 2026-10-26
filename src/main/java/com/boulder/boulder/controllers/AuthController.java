package com.boulder.boulder.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.boulder.boulder.dto.RegistroDTO;
import com.boulder.boulder.dto.UsuarioDTO;
import com.boulder.boulder.services.UsuarioService;

import jakarta.validation.Valid;

/**
 * Controlador REST para las operaciones públicas de autenticación.
 *
 * <p>
 * El inicio de sesión ({@code POST /api/auth/login}) no se declara aquí: lo
 * atiende {@link com.boulder.boulder.config.JwtAuthenticationFilter} a nivel de
 * filtro de Spring Security. Este controlador solo cubre el auto-registro.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Auto-registro público. La cuenta se crea inactiva y sin roles; hasta que un
     * administrador la habilite, el login responderá "usuario inactivo".
     */
    @PostMapping("/register")
    public ResponseEntity<UsuarioDTO> registrar(@Valid @RequestBody RegistroDTO dto) {
        log.info("HTTP POST - Auto-registro de la cuenta: {}", dto.getUsuario());
        return new ResponseEntity<>(usuarioService.registrar(dto), HttpStatus.CREATED);
    }
}
