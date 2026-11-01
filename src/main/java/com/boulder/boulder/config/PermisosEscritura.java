package com.boulder.boulder.config;

import java.util.Set;

/**
 * Quién puede escribir en el API operacional, de logueo, de importación y de fotos.
 *
 * <ul>
 *   <li>Lectura: cualquier rol con sesión.</li>
 *   <li>{@code ASISTENTE_GEOLOGIA}: registra y corrige partes, logueo, cargas y fotos.</li>
 *   <li>{@code GEOLOGO}: además borra partes y mantiene los datos maestros.</li>
 *   <li>{@code ADMIN}: todo. {@code VISITANTE}: solo lectura.</li>
 * </ul>
 */
public final class PermisosEscritura {

    private static final String ADMIN = "ROLE_ADMIN";
    private static final String GEOLOGO = "ROLE_GEOLOGO";
    private static final String ASISTENTE = "ROLE_ASISTENTE_GEOLOGIA";

    /** Datos maestros de la campaña dentro de /api/operacional. */
    private static final Set<String> MAESTROS = Set.of(
            "sondajes", "maquinas", "plataformas", "programas-sondaje", "collares");

    private PermisosEscritura() {
    }

    /**
     * @param metodo método HTTP
     * @param ruta ruta de la petición, sin contexto
     * @param autoridades autoridades del usuario ({@code ROLE_...})
     * @return si la petición está permitida
     */
    public static boolean permite(String metodo, String ruta, Set<String> autoridades) {
        if ("GET".equals(metodo) || "HEAD".equals(metodo) || autoridades.contains(ADMIN)) {
            return true;
        }
        boolean geologo = autoridades.contains(GEOLOGO);
        if (ruta.startsWith("/api/operacional/")
                && ("DELETE".equals(metodo) || MAESTROS.contains(recurso(ruta)))) {
            return geologo;
        }
        return geologo || autoridades.contains(ASISTENTE);
    }

    /** Primer segmento después de /api/operacional/. */
    private static String recurso(String ruta) {
        String resto = ruta.substring("/api/operacional/".length());
        int barra = resto.indexOf('/');
        return barra < 0 ? resto : resto.substring(0, barra);
    }
}
