package com.boulder.boulder.config;

import java.util.Set;

/**
 * Quién puede escribir en el API operacional, de logueo, de importación y de fotos.
 */
public final class PermisosEscritura {

    private PermisosEscritura() {
    }

    /**
     * @param metodo método HTTP
     * @param ruta ruta de la petición, sin contexto
     * @param autoridades autoridades del usuario ({@code ROLE_...})
     * @return si la petición está permitida
     */
    public static boolean permite(String metodo, String ruta, Set<String> autoridades) {
        return false;
    }
}
