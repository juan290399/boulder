package com.boulder.boulder.entities;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Lo que tienen en común las cinco plantillas del esquema {@code logueo}.
 *
 * <p>
 * Existe para que la sincronización trate igual a las cinco: saber de qué
 * sondaje es una fila —para avisar a quien lo está mirando— y si está borrada,
 * sin repetir cinco veces el mismo código.
 * </p>
 */
public interface RegistroLogueo {

    UUID getSondajeId();

    OffsetDateTime getEliminadoEn();
}
