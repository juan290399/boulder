package com.boulder.boulder.services;

import java.util.List;
import java.util.UUID;

import com.boulder.boulder.dto.CatalogoGeologiaDTO;

/**
 * Los diccionarios del esquema {@code geologia}: litologías, alteraciones,
 * minerales, intensidades, estilos, zonas y demás catálogos.
 *
 * <p>El tablet y las pantallas de captura solo los leen. El administrador los
 * mantiene desde la pantalla de diccionarios (RF04, CU06): los códigos se eligen
 * de aquí y no se escriben a mano, que es lo que evita «Clt», «CLT» y «clt» para
 * la misma roca.</p>
 *
 * @author jvelazco
 * @version 1.1
 * @since 1.0
 */
public interface CatalogoGeologiaService {

    /** Los veintiún catálogos aplanados en una sola lista con discriminador. */
    List<CatalogoGeologiaDTO> listarTodos();

    /**
     * Agrega un código a un catálogo.
     *
     * @param tipo  nombre del catálogo (la tabla de {@code geologia}, p. ej. {@code lit_diccionario})
     * @param datos código, nombre y, según el catálogo, grupo y color
     * @return la entrada creada, con su identificador
     */
    CatalogoGeologiaDTO crear(String tipo, CatalogoGeologiaDTO datos);

    /** Corrige el código, el nombre, el grupo o el color de una entrada existente. */
    CatalogoGeologiaDTO actualizar(String tipo, UUID id, CatalogoGeologiaDTO datos);

    /**
     * Activa o desactiva una entrada. Desactivar no borra: lo ya registrado con ese
     * código sigue intacto, solo deja de ofrecerse en las pantallas de captura.
     */
    void cambiarEstado(String tipo, UUID id, boolean activo);
}
