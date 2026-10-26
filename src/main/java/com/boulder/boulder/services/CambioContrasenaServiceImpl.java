package com.boulder.boulder.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.boulder.boulder.repositories.UsuarioRepository;

@Service
public class CambioContrasenaServiceImpl implements CambioContrasenaService {

    public CambioContrasenaServiceImpl(UsuarioRepository usuarios, PasswordEncoder codificador) {
    }

    @Override
    public void cambiar(String usuario, String actual, String nueva) {
        throw new UnsupportedOperationException("pendiente");
    }
}
