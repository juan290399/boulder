package com.boulder.boulder.eventos;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Aviso de que cambiaron datos de uno o más sondajes.
 *
 * <p>
 * Lleva lo justo para que cada pantalla decida si le importa —qué recurso y de
 * qué sondajes— y no los datos en sí. La pantalla que está mirando ese sondaje
 * vuelve a pedirlos por el endpoint de siempre, con sus permisos de siempre.
 * Mandar las filas por este canal duplicaría la lógica de lectura y abriría
 * una segunda puerta a los datos.
 * </p>
 *
 * @param recurso  qué cambió: {@code quicklog}, {@code litologia}, {@code avance-logueo}…
 * @param sondajes sondajes afectados
 * @param origen   quién lo cambió: {@code tablet}, {@code web} o {@code importacion}
 * @param filas    cuántas filas trajo el cambio
 * @param momento  cuándo se confirmó en la base
 */
public record CambioDatos(String recurso, Set<UUID> sondajes, String origen, int filas, OffsetDateTime momento) {

    public static CambioDatos de(String recurso, Set<UUID> sondajes, String origen, int filas) {
        return new CambioDatos(recurso, sondajes, origen, filas, OffsetDateTime.now());
    }
}
