package com.boulder.boulder.dominio;

import java.util.Optional;

/**
 * Código canónico de un sondaje: {@code DDH-<PROYECTO>-<SECTOR>-<NNN>-<AA>}.
 */
public final class CodigoSondaje {

    private CodigoSondaje() {
    }

    public static Optional<String> normalizar(String escrito) {
        throw new UnsupportedOperationException("pendiente");
    }

    public static boolean mismoSondaje(String a, String b) {
        throw new UnsupportedOperationException("pendiente");
    }
}
