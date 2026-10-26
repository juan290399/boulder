package com.boulder.boulder.config;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boulder.boulder.entities.Usuario;
import com.boulder.boulder.entities.UsuarioRol;
import com.boulder.boulder.repositories.UsuarioRepository;
import com.boulder.boulder.repositories.UsuarioRolRepository;

@Service
public class MyUserDetailsService implements UserDetailsService, UserDetailsPasswordService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioRolRepository usuarioRolRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {

        Usuario user = usuarioRepository.findByUsuario(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        List<UsuarioRol> userRoles = usuarioRolRepository.findByUsuarioId(user.getId());

        List<SimpleGrantedAuthority> authorities = userRoles.stream()
                .map(ur -> ur.getRol().getCodigo())
                .map(rolNombre -> new SimpleGrantedAuthority("ROLE_" + rolNombre))
                .collect(Collectors.toList());

        return new MyUserMain(user, authorities);
    }

    /**
     * Guarda la contraseña ya recodificada.
     *
     * <p>
     * Spring la llama tras un login correcto cuando el hash guardado usa un
     * algoritmo distinto del vigente: es el paso que convierte las contraseñas
     * en texto plano a BCrypt la primera vez que cada usuario entra.
     * </p>
     */
    @Override
    @Transactional
    public UserDetails updatePassword(UserDetails user, String newPassword) {
        Usuario usuario = usuarioRepository.findByUsuario(user.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + user.getUsername()));
        usuario.setContrasenia(newPassword);
        usuarioRepository.save(usuario);
        return loadUserByUsername(user.getUsername());
    }
}
