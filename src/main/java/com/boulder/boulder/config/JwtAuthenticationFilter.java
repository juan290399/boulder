package com.boulder.boulder.config;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.boulder.boulder.entities.Usuario;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Atiende {@code POST /api/auth/login}: valida usuario y contraseña y responde con
 * un token JWT.
 *
 * <p>Antes de autenticar consulta el {@link LimiteIntentosLogin}: una cuenta con
 * demasiados fallos recientes recibe 429 sin que se llegue a comparar la
 * contraseña. Cada intento queda en la bitácora de seguridad, nunca con la
 * contraseña.</p>
 */
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    /** Bitácora de seguridad; Logback la envía a su propio archivo. */
    private static final Logger AUDITORIA = LoggerFactory.getLogger("com.boulder.boulder.seguridad");

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final LimiteIntentosLogin limite;
    private final ObjectMapper json = new ObjectMapper();

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, JwtUtils jwtUtils,
                                   LimiteIntentosLogin limite) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.limite = limite;
    }

    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws AuthenticationException {
        Usuario credenciales;
        try {
            credenciales = json.readValue(request.getInputStream(), Usuario.class);
        } catch (IOException e) {
            throw new RuntimeException("Error en la lectura del flujo de credenciales", e);
        }
        String usuario = credenciales.getUsuario();

        if (limite.bloqueado(usuario)) {
            AUDITORIA.warn("login bloqueado usuario={} ip={}", seguro(usuario), request.getRemoteAddr());
            throw new CuentaBloqueadaException();
        }
        try {
            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(usuario, credenciales.getContrasenia()));
        } catch (AuthenticationException e) {
            limite.registrarFallo(usuario);
            AUDITORIA.warn("login fallido usuario={} ip={} motivo={}",
                    seguro(usuario), request.getRemoteAddr(), e.getClass().getSimpleName());
            throw e;
        }
    }

    @Override
    protected void successfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            Authentication authResult
    ) throws IOException, ServletException {
        limite.registrarExito(authResult.getName());
        AUDITORIA.info("login correcto usuario={} ip={}", seguro(authResult.getName()), request.getRemoteAddr());

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json.writeValueAsString(Map.of("token", jwtUtils.generateToken(authResult))));
    }

    @Override
    protected void unsuccessfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException failed
    ) throws IOException, ServletException {
        if (!(failed instanceof CuentaBloqueadaException)) {
            super.unsuccessfulAuthentication(request, response, failed);
            return;
        }
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json.writeValueAsString(Map.of("mensaje",
                "Demasiados intentos fallidos. La cuenta queda bloqueada 15 minutos.")));
    }

    /**
     * El usuario lo escribe quien intenta entrar: se le quitan los saltos de línea
     * para que no pueda fabricar líneas falsas en la bitácora (CWE-117) y se
     * recorta para que no la llene.
     */
    static String seguro(String usuario) {
        return StringUtils.abbreviate(StringUtils.replaceChars(String.valueOf(usuario), "\r\n\t", "___"), 64);
    }

    /** La cuenta superó el límite de intentos fallidos. */
    static class CuentaBloqueadaException extends AuthenticationException {
        CuentaBloqueadaException() {
            super("Demasiados intentos fallidos");
        }
    }
}
