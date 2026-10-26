package com.boulder.boulder.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boulder.boulder.entities.Usuario;
import com.boulder.boulder.repositories.UsuarioRepository;
import com.google.common.base.Ticker;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Responde si la cuenta de un token sigue existiendo y activa.
 *
 * <p>El JWT es autocontenido: firmado y vigente 24 horas, sirve aunque la cuenta
 * se haya desactivado o borrado después de emitirlo. Sin esta comprobación,
 * desactivar a un usuario (RF03) no le corta el acceso hasta que el token vence.
 * La respuesta se guarda unos segundos en una caché de Guava para no consultar
 * la base en cada petición, y se olvida al cambiar el estado de la cuenta.</p>
 */
@Component
public class CuentasActivas {

    private final LoadingCache<String, Boolean> activas;

    @Autowired
    public CuentasActivas(UsuarioRepository usuarios) {
        this(usuarios, Duration.ofSeconds(30), Ticker.systemTicker());
    }

    public CuentasActivas(UsuarioRepository usuarios, Duration recordar, Ticker reloj) {
        this.activas = CacheBuilder.newBuilder()
                .expireAfterWrite(recordar)
                .maximumSize(1_000)
                .ticker(reloj)
                .build(CacheLoader.from((String usuario) -> usuarios.findByUsuario(usuario)
                        .map(Usuario::getEstado)
                        .map(Boolean.TRUE::equals)
                        .orElse(false)));
    }

    /** @return {@code true} si la cuenta existe y está activa */
    public boolean activa(String usuario) {
        return activas.getUnchecked(usuario);
    }

    /** Descarta lo recordado de una cuenta, para que el próximo pedido la relea. */
    public void olvidar(String usuario) {
        activas.invalidate(usuario);
    }
}
