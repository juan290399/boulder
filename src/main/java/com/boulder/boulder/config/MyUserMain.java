package com.boulder.boulder.config;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.boulder.boulder.entities.Usuario;

public class MyUserMain implements UserDetails {

    private final Usuario usuario;
    private final List<SimpleGrantedAuthority> authorities;

    public MyUserMain(Usuario usuario, List<SimpleGrantedAuthority> authorities) {
        this.usuario = usuario;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return usuario.getContrasenia();
    }

    @Override
    public String getUsername() {
        return usuario.getUsuario();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Refleja {@code base.usuario.estado}. Si esto devuelve siempre true, el
     * flag de cuenta activa queda decorativo y los usuarios dados de baja
     * pueden iniciar sesión igual.
     */
    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(usuario.getEstado());
    }

    public Usuario getUsuarioEntity() {
        return usuario;
    }
}