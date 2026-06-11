package com.boulder.boulder.services;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boulder.boulder.dto.PlataformaDTO;
import com.boulder.boulder.entities.Plataforma;
import com.boulder.boulder.entities.Proyecto;
import com.boulder.boulder.repositories.PlataformaRepository;
import com.boulder.boulder.repositories.ProyectoRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * Implementación de los servicios de negocio para la gestión de plataformas.
 *
 * <p>
 * Controla el ciclo de vida de las plataformas de perforación, coordinando
 * su asociación obligatoria a un proyecto minero específico.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Service
public class PlataformaServiceImpl implements PlataformaService {

    private static final Logger log = LoggerFactory.getLogger(PlataformaServiceImpl.class);

    @Autowired
    private PlataformaRepository plataformaRepository;

    @Autowired
    private ProyectoRepository proyectoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PlataformaDTO> listarTodas() {
        log.debug("Consultando listado total de plataformas geológicas");
        return plataformaRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PlataformaDTO obtenerPorId(UUID id) {
        log.debug("Buscando plataforma con ID único: {}", id);
        Plataforma plataforma = plataformaRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Plataforma no localizada con ID: {}", id);
                    return new EntityNotFoundException("Plataforma no encontrada con el ID provisto.");
                });
        return convertToDto(plataforma);
    }

    @Override
    @Transactional(readOnly = true)
    public PlataformaDTO obtenerPorCodigo(String codigo) {
        log.debug("Buscando plataforma con código: {}", codigo);

        Plataforma plataforma = plataformaRepository.findByCodigo(codigo)
                .orElseThrow(() -> {
                    log.warn("Plataforma no encontrada con código: {}", codigo);
                    return new EntityNotFoundException("Plataforma no encontrada con código: " + codigo);
                });
        return convertToDto(plataforma);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlataformaDTO> listarPorProyecto(UUID proyectoId) {
        log.debug("Buscando plataformas asignadas al proyecto ID: {}", proyectoId);
        
        return plataformaRepository.findByProyectoId(proyectoId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PlataformaDTO guardar(PlataformaDTO dto) {
        log.info("Registrando nueva plataforma. Código: {}, Proyecto ID: {}", dto.getCodigo(), dto.getProyectoId());
        try {
            Proyecto proyecto = proyectoRepository.findById(dto.getProyectoId())
                    .orElseThrow(() -> new EntityNotFoundException("El Proyecto asociado con ID " + dto.getProyectoId() + " no existe."));

            Plataforma plataforma = new Plataforma();
            plataforma.setCodigo(dto.getCodigo());
            plataforma.setNombre(dto.getNombre());
            plataforma.setEstado(dto.getEstado() != null ? dto.getEstado() : "DISPONIBLE");
            plataforma.setProyecto(proyecto);

            Plataforma guardada = plataformaRepository.save(plataforma);
            log.info("Plataforma guardada correctamente. ID: {}, Código: {}", guardada.getId(), guardada.getCodigo());
            return convertToDto(guardada);
        } catch (Exception e) {
            log.error("Error al registrar plataforma con código: {}", dto.getCodigo(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public PlataformaDTO actualizar(UUID id, PlataformaDTO dto) {
        log.info("Actualizando plataforma con ID: {}", id);
        try {
            Plataforma plataforma = plataformaRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Intento de actualizar plataforma inexistente con ID: {}", id);
                        return new EntityNotFoundException("Plataforma no encontrada.");
                    });

            if (dto.getProyectoId() != null && !plataforma.getProyecto().getId().equals(dto.getProyectoId())) {
                Proyecto nuevoProyecto = proyectoRepository.findById(dto.getProyectoId())
                        .orElseThrow(() -> new EntityNotFoundException("El nuevo Proyecto asociado no existe."));
                plataforma.setProyecto(nuevoProyecto);
            }

            plataforma.setNombre(dto.getNombre());
            plataforma.setEstado(dto.getEstado());

            Plataforma actualizada = plataformaRepository.save(plataforma);
            log.info("Plataforma {} actualizada correctamente", actualizada.getCodigo());
            return convertToDto(actualizada);
        } catch (Exception e) {
            log.error("Error actualizando la plataforma con ID: {}", id, e);
            throw e;
        }
    }

    private PlataformaDTO convertToDto(Plataforma plataforma) {
        PlataformaDTO dto = new PlataformaDTO();
        dto.setId(plataforma.getId());
        dto.setNumeroInterno(plataforma.getNumeroInterno());
        dto.setCodigo(plataforma.getCodigo());
        dto.setNombre(plataforma.getNombre());
        dto.setEstado(plataforma.getEstado());
        
        if (plataforma.getProyecto() != null) {
            dto.setProyectoId(plataforma.getProyecto().getId());
        }
        return dto;
    }
}