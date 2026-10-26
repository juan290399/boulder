package com.boulder.boulder.dto.importacion;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Una plantilla que el importador sabe leer.
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Data
@AllArgsConstructor
public class PlantillaDTO {

    /** Identificador que viaja en la URL. */
    private String clave;

    /** Nombre del .xlsx que corresponde a esta plantilla. */
    private String archivo;

    /** Qué carga, en una línea. */
    private String descripcion;
}
