package com.boulder.boulder.dto.importacion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Un renglón que el importador no pudo cargar, con el motivo.
 *
 * <p>
 * El número de fila es el de Excel —cabecera incluida, empezando en 1— y no el
 * índice del recorrido: quien corrige la plantilla trabaja sobre la hoja
 * abierta, así que el número tiene que ser el que ve en el margen izquierdo.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorFilaDTO {

    /** Número de fila tal como lo muestra Excel. */
    private int fila;

    /** Identificación legible del renglón, para ubicarlo sin abrir el archivo. */
    private String referencia;

    /** Motivos acumulados, ya unidos en una sola frase. */
    private String motivo;
}
