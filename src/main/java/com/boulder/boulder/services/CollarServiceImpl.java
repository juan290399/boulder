package com.boulder.boulder.services;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boulder.boulder.dto.CollarDTO;
import com.boulder.boulder.entities.Collar;
import com.boulder.boulder.repositories.CollarRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * Implementación de los servicios de negocio para la gestión topográfica de Collares.
 *
 * <p>
 * Valida la consistencia de los datos medidos en la boca del pozo y asegura el 
 * desacoplamiento de objetos relacionales para el óptimo consumo desde la interfaz de Angular.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Service
public class CollarServiceImpl implements CollarService {

    private static final Logger log = LoggerFactory.getLogger(CollarServiceImpl.class);

    @Autowired
    private CollarRepository collarRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CollarDTO> listarTodos() {
        log.debug("Consultando listado general de collares topográficos registrados");
        return collarRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CollarDTO obtenerPorId(UUID id) {
        log.debug("Buscando registro de collar por ID único: {}", id);
        Collar collar = collarRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Collar topográfico no localizado con ID: {}", id);
                    return new EntityNotFoundException("Registro de collar no encontrado con el ID provisto.");
                });
        return convertToDto(collar);
    }

    @Override
    @Transactional(readOnly = true)
    public CollarDTO obtenerPorCodigo(String codigo) {
        log.debug("Buscando collar por código identificador único: {}", codigo);
        Collar collar = collarRepository.findByCodigo(codigo)
                .orElseThrow(() -> {
                    log.warn("Collar topográfico no localizado con código: {}", codigo);
                    return new EntityNotFoundException("No se encontró ningún registro de collar con el código: " + codigo);
                });
        return convertToDto(collar);
    }

    @Override
    @Transactional
    public CollarDTO guardar(CollarDTO dto) {
        log.info("Registrando nueva boca de pozo (Collar) en el sistema. Código: {}", dto.getCodigo());
        try {
            Collar collar = new Collar();
            
            collar.setCodigo(dto.getCodigo());
            collar.setNombre(dto.getNombre());
            collar.setEste(dto.getEste());
            collar.setNorte(dto.getNorte());
            collar.setElevacion(dto.getElevacion());
            collar.setFechaMedicion(dto.getFechaMedicion());
            collar.setCampania(dto.getCampania());
            collar.setNotas(dto.getNotas());

            Collar guardado = collarRepository.save(collar);
            log.info("Collar registrado exitosamente con ID: {}, Código: {}", guardado.getId(), guardado.getCodigo());
            return convertToDto(guardado);
        } catch (Exception e) {
            log.error("Fallo al registrar el collar con código: {}", dto.getCodigo(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public CollarDTO actualizar(UUID id, CollarDTO dto) {
        log.info("Actualizando metadatos del collar con ID: {}", id);
        try {
            Collar collar = collarRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Intento de modificar collar inexistente con ID: {}", id);
                        return new EntityNotFoundException("Collar no existente en la base de datos.");
                    });

            collar.setNombre(dto.getNombre());
            collar.setEste(dto.getEste());
            collar.setNorte(dto.getNorte());
            collar.setElevacion(dto.getElevacion());
            collar.setFechaMedicion(dto.getFechaMedicion());
            collar.setCampania(dto.getCampania());
            collar.setNotas(dto.getNotas());

            Collar actualizado = collarRepository.save(collar);
            log.info("Collar '{}' actualizado con éxito en el sistema", actualizado.getCodigo());
            return convertToDto(actualizado);
        } catch (Exception e) {
            log.error("Error al modificar el registro del collar ID: {}", id, e);
            throw e;
        }
    }

    private CollarDTO convertToDto(Collar collar) {
        CollarDTO dto = new CollarDTO();
        dto.setId(collar.getId());
        dto.setNumeroInterno(collar.getNumeroInterno());
        dto.setCodigo(collar.getCodigo());
        dto.setNombre(collar.getNombre());
        dto.setEste(collar.getEste());
        dto.setNorte(collar.getNorte());
        dto.setElevacion(collar.getElevacion());
        dto.setFechaMedicion(collar.getFechaMedicion());
        dto.setCampania(collar.getCampania());
        dto.setNotas(collar.getNotas());

        return dto;
    }
}