package com.boulder.boulder.services.importacion;

import lombok.Getter;

/**
 * Se lanza cuando la base rechaza un renglón que la validación dejó pasar.
 *
 * <p>
 * Cuando ocurre no se carga nada: la transacción se deshace entera. Es a
 * propósito. Una restricción que salta en mitad de la carga significa que el
 * importador no entendió algo del archivo, y dejar media plantilla adentro
 * obligaría a adivinar por dónde iba para retomar.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Getter
public class ImportacionAbortadaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String hoja;
    private final int fila;

    public ImportacionAbortadaException(String hoja, int fila, String motivo, Throwable causa) {
        super("La base rechazó la fila " + fila + " de la hoja " + hoja + ": " + motivo, causa);
        this.hoja = hoja;
        this.fila = fila;
    }
}
