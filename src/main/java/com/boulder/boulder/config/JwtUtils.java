package com.boulder.boulder.config;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.boulder.boulder.entities.Usuario;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Authentication auth) {

        String username = auth.getName();

        // Solo las authorities de rol. Spring Security añade además factores de
        // autenticación (FACTOR_PASSWORD, etc.); si se copian al claim acaban
        // apareciendo como roles en el cliente.
        List<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .toList();

        return Jwts.builder()
                .subject(username)
                .claim("nombre", nombreVisible(auth, username))
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Nombre y apellidos para mostrar en la interfaz.
     *
     * <p>
     * Va en el token a propósito: la ficha del usuario cuelga de
     * {@code /api/base/usuarios/**}, que es exclusivo de ADMIN, así que un
     * geólogo no puede consultar ni su propio perfil para saber cómo se llama.
     * </p>
     */
    private String nombreVisible(Authentication auth, String porDefecto) {
        if (auth.getPrincipal() instanceof MyUserMain principal) {
            Usuario usuario = principal.getUsuarioEntity();
            String completo = (usuario.getNombres() + " "
                    + (usuario.getApellidos() != null ? usuario.getApellidos() : "")).trim();
            if (!completo.isEmpty()) {
                return completo;
            }
        }
        return porDefecto;
    }

    public Claims getAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getUsernameFromToken(String token) {
        return getAllClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        try {
            getAllClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");

        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}