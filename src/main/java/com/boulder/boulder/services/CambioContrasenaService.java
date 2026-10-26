package com.boulder.boulder.services;

/**
 * Cambio de contraseña del usuario que inició sesión (RF02, CU02).
 */
public interface CambioContrasenaService {

    void cambiar(String usuario, String actual, String nueva);
}
