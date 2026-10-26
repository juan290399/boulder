package com.boulder.boulder.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boulder.boulder.dto.CatalogoGeologiaDTO;
import com.boulder.boulder.repositories.CatalogoGeologiaRepository;

/**
 * Implementación del servicio de catálogos geológicos.
 *
 * <p>No convierte nada: el repositorio ya devuelve el DTO, porque estas tablas
 * no tienen entidad JPA detrás. Ver {@link CatalogoGeologiaRepository} para el
 * porqué.</p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Service
public class CatalogoGeologiaServiceImpl implements CatalogoGeologiaService {

    private static final Logger log = LoggerFactory.getLogger(CatalogoGeologiaServiceImpl.class);

    @Autowired
    private CatalogoGeologiaRepository catalogoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CatalogoGeologiaDTO> listarTodos() {
        List<CatalogoGeologiaDTO> catalogos = catalogoRepository.listarTodos();
        log.debug("Entregando {} entradas de catálogo geológico", catalogos.size());
        return catalogos;
    }
}
