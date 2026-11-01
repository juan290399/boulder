package com.boulder.boulder.dto;

import java.util.List;
import java.util.UUID;

/**
 * Resultado de la subida de un lote de logueo: cuántas filas se guardaron y
 * cuáles se rechazaron, con su motivo.
 *
 * @param guardadas filas guardadas
 * @param rechazadas filas rechazadas; vacía si el lote entró completo
 */
public record ResultadoSubidaDTO(int guardadas, List<Rechazo> rechazadas) {

    /**
     * Fila rechazada.
     *
     * @param indice posición de la fila en el lote, desde 0
     * @param id id de la fila, o {@code null} si llegó sin id
     * @param motivo causa del rechazo
     */
    public record Rechazo(int indice, UUID id, String motivo) {
    }

    public boolean completo() {
        return rechazadas.isEmpty();
    }
}
