package com.boulder.boulder.dto.importacion;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * Lo que pasó con una hoja de la plantilla.
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Data
public class ResultadoHojaDTO {

    /** Nombre de la hoja en el libro. */
    private String hoja;

    /** Tabla de destino, para que el resultado diga dónde fue a parar el dato. */
    private String destino;

    /** Renglones con datos, sin contar la cabecera ni las filas en blanco. */
    private int leidas;

    /** Renglones válidos y nuevos. En modo simulación, los que se cargarían. */
    private int aceptadas;

    /**
     * Renglones válidos que ya estaban en la base, o repetidos dentro del
     * mismo archivo. No son un error: son lo que hace que volver a subir la
     * misma plantilla no duplique nada.
     */
    private int omitidas;

    /** Renglones rechazados, con el detalle en {@link #errores}. */
    private int rechazadas;

    /** El motivo de cada rechazo, uno por renglón. */
    private List<ErrorFilaDTO> errores = new ArrayList<>();

    /** Aviso cuando la hoja no aparece en el libro. */
    private String advertencia;
}
