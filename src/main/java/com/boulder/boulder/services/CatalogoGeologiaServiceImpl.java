package com.boulder.boulder.services;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boulder.boulder.dto.CatalogoGeologiaDTO;
import com.boulder.boulder.repositories.CatalogoGeologiaRepository;
import com.boulder.boulder.repositories.CatalogoGeologiaRepository.Estructura;

import jakarta.persistence.EntityNotFoundException;

/**
 * Implementación del servicio de catálogos geológicos.
 *
 * <p>Las reglas de negocio viven aquí y el acceso a datos en
 * {@link CatalogoGeologiaRepository} (patrón DAO): el servicio decide si un
 * código es válido; el repositorio solo sabe escribirlo. Las dependencias entran
 * por el constructor, de modo que las reglas se prueban con un repositorio
 * simulado.</p>
 *
 * @author jvelazco
 * @version 1.1
 * @since 1.0
 */
@Service
public class CatalogoGeologiaServiceImpl implements CatalogoGeologiaService {

    private static final Logger log = LoggerFactory.getLogger(CatalogoGeologiaServiceImpl.class);
    private static final int LARGO_NOMBRE = 150;

    private final CatalogoGeologiaRepository catalogoRepository;

    public CatalogoGeologiaServiceImpl(CatalogoGeologiaRepository catalogoRepository) {
        this.catalogoRepository = catalogoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CatalogoGeologiaDTO> listarTodos() {
        List<CatalogoGeologiaDTO> catalogos = catalogoRepository.listarTodos();
        log.debug("Entregando {} entradas de catálogo geológico", catalogos.size());
        return catalogos;
    }

    @Override
    public Map<String, Estructura> estructura() {
        return catalogoRepository.estructura();
    }

    @Override
    @Transactional
    public CatalogoGeologiaDTO crear(String tipo, CatalogoGeologiaDTO datos) {
        CatalogoGeologiaDTO limpio = validar(tipo, datos);
        limpio.setId(catalogoRepository.insertar(tipo, limpio));
        limpio.setEstaActivo(true);
        log.info("Catálogo {}: alta del código {}", tipo, limpio.getCodigo());
        return limpio;
    }

    @Override
    @Transactional
    public CatalogoGeologiaDTO actualizar(String tipo, UUID id, CatalogoGeologiaDTO datos) {
        CatalogoGeologiaDTO limpio = validar(tipo, datos);
        if (catalogoRepository.actualizar(tipo, id, limpio) == 0) {
            throw new EntityNotFoundException("No existe esa entrada en el catálogo " + tipo);
        }
        limpio.setId(id);
        log.info("Catálogo {}: código {} actualizado", tipo, limpio.getCodigo());
        return limpio;
    }

    @Override
    @Transactional
    public void cambiarEstado(String tipo, UUID id, boolean activo) {
        Estructura e = estructura(tipo);
        if (!e.tiene("esta_activo")) {
            throw new IllegalStateException("El catálogo " + tipo + " no admite desactivación.");
        }
        if (catalogoRepository.cambiarEstado(tipo, id, activo) == 0) {
            throw new EntityNotFoundException("No existe esa entrada en el catálogo " + tipo);
        }
        log.info("Catálogo {}: entrada {} {}", tipo, id, activo ? "activada" : "desactivada");
    }

    /** Aplica las reglas y devuelve una copia normalizada; no modifica lo que llegó. */
    private CatalogoGeologiaDTO validar(String tipo, CatalogoGeologiaDTO datos) {
        Estructura e = estructura(tipo);

        String codigo = StringUtils.upperCase(StringUtils.trimToEmpty(datos.getCodigo()));
        if (codigo.isEmpty() || !StringUtils.isAlphanumeric(codigo) || codigo.length() > e.largoCodigo()) {
            throw new IllegalArgumentException("El código debe ser alfanumérico, sin espacios ni guiones, y de "
                    + e.largoCodigo() + " caracteres como máximo.");
        }
        String nombre = StringUtils.normalizeSpace(datos.getNombre());
        if (StringUtils.isBlank(nombre) || nombre.length() > LARGO_NOMBRE) {
            throw new IllegalArgumentException("El nombre es obligatorio y tiene " + LARGO_NOMBRE + " caracteres como máximo.");
        }
        if (e.tiene("grupo_id") && datos.getGrupoId() == null) {
            throw new IllegalArgumentException("Elige el grupo litológico al que pertenece.");
        }
        validarColor(e, datos);

        CatalogoGeologiaDTO limpio = new CatalogoGeologiaDTO();
        limpio.setTipo(tipo);
        limpio.setCodigo(codigo);
        limpio.setNombre(nombre);
        limpio.setGrupoId(e.tiene("grupo_id") ? datos.getGrupoId() : null);
        limpio.setColorR(datos.getColorR());
        limpio.setColorG(datos.getColorG());
        limpio.setColorB(datos.getColorB());
        return limpio;
    }

    private static void validarColor(Estructura e, CatalogoGeologiaDTO d) {
        List<Short> canales = Stream.of(d.getColorR(), d.getColorG(), d.getColorB()).toList();
        long presentes = canales.stream().filter(c -> c != null).count();
        if (presentes == 0) {
            return;
        }
        if (!e.tiene("color_r")) {
            throw new IllegalArgumentException("Este catálogo no lleva color.");
        }
        if (presentes != 3 || canales.stream().anyMatch(c -> c < 0 || c > 255)) {
            throw new IllegalArgumentException("El color lleva los tres canales (R, G, B), cada uno entre 0 y 255.");
        }
    }

    private Estructura estructura(String tipo) {
        Estructura e = tipo == null ? null : catalogoRepository.estructura().get(tipo);
        if (e == null) {
            throw new IllegalArgumentException("Catálogo desconocido: " + StringUtils.abbreviate(tipo, 40));
        }
        return e;
    }
}
