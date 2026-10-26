package com.boulder.boulder.services;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.boulder.boulder.dto.importacion.PlantillaDTO;
import com.boulder.boulder.dto.importacion.ResultadoImportacionDTO;

/**
 * Carga masiva desde las plantillas de Excel.
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
public interface ImportacionService {

    /** Las plantillas que el importador sabe leer. */
    List<PlantillaDTO> plantillas();

    /**
     * Lee el libro y, si se pide, lo carga.
     *
     * @param plantilla clave de la plantilla, por ejemplo {@code perforacion}
     * @param archivo   el .xlsx subido
     * @param aplicar   {@code false} para simular: se valida todo y no se
     *                  escribe nada
     */
    ResultadoImportacionDTO importar(String plantilla, MultipartFile archivo, boolean aplicar);
}
