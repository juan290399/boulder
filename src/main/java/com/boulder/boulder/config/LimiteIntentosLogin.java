package com.boulder.boulder.config;

import java.time.Duration;

import com.google.common.base.Ticker;

/**
 * Limita los intentos fallidos de inicio de sesión por usuario.
 */
public class LimiteIntentosLogin {

    public LimiteIntentosLogin(int maxIntentos, Duration bloqueo, Ticker reloj) {
    }

    public boolean bloqueado(String usuario) {
        throw new UnsupportedOperationException("pendiente");
    }

    public void registrarFallo(String usuario) {
        throw new UnsupportedOperationException("pendiente");
    }

    public void registrarExito(String usuario) {
        throw new UnsupportedOperationException("pendiente");
    }
}
