package com.boulder.boulder.services.importacion;

import org.apache.poi.ss.usermodel.Workbook;

import com.boulder.boulder.dto.importacion.ResultadoImportacionDTO;

/**
 * Un importador por plantilla.
 *
 * <p>
 * Cada implementación conoce una sola plantilla: qué hojas tiene, a qué tabla
 * va cada una y cuál es la clave natural que evita el duplicado. El despacho
 * por {@link #clave()} lo resuelve
 * {@link com.boulder.boulder.services.ImportacionServiceImpl}.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
public interface ImportadorPlantilla {

    /** Identificador que viaja en la URL, por ejemplo {@code perforacion}. */
    String clave();

    /** Nombre del archivo de plantilla que corresponde a este importador. */
    String archivoPlantilla();

    /** Qué carga, en una línea, para el listado de plantillas. */
    String descripcion();

    /**
     * Recorre el libro y arma el informe.
     *
     * @param libro    el .xlsx ya abierto
     * @param catalogos resolución de códigos, compartida por todas las hojas
     * @param aplicar  {@code false} para simular: se valida y no se escribe
     */
    ResultadoImportacionDTO importar(Workbook libro, Catalogos catalogos, boolean aplicar);
}
