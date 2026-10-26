package com.boulder.boulder.dto;

import java.util.UUID;

import lombok.Data;

/**
 * Una fila de cualquiera de los diccionarios del esquema {@code geologia}.
 *
 * <p>Las veintiuna tablas de catálogo comparten forma —{@code id},
 * {@code numero_interno}, {@code codigo}, {@code nombre}— y se diferencian solo
 * en tres columnas opcionales: el grupo litológico, el color RGB con el que se
 * pinta la columna de profundidad, y la baja lógica. Por eso viajan aplanadas en
 * una sola lista con {@link #tipo} como discriminador, en vez de exponer
 * veintiún endpoints para traer 348 filas en total.</p>
 *
 * <p>El cliente Android las guarda exactamente así, en una única tabla
 * {@code catalogo}; el nombre de cada campo coincide con el de su propiedad
 * allí, que es lo que hace que no haga falta traducir nada al recibirlas.</p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Data
public class CatalogoGeologiaDTO {

    private UUID id;

    /** Nombre de la tabla de origen: {@code lit_diccionario}, {@code est_relleno}… */
    private String tipo;

    private String codigo;
    private String nombre;

    /** Solo {@code lit_diccionario}: el grupo litológico al que pertenece la roca. */
    private UUID grupoId;

    // Solo los cuatro diccionarios principales (lit, alt, min, est).
    private Short colorR;
    private Short colorG;
    private Short colorB;

    private Integer numeroInterno;

    /**
     * Baja lógica. Las trece tablas que no la tienen llegan siempre con
     * {@code true}: sus valores no se dan de baja nunca, y devolver
     * {@code null} obligaría al cliente a distinguir dos formas de "está vigente".
     */
    private Boolean estaActivo;
}
