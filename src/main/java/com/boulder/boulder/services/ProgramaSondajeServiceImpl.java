package com.boulder.boulder.services;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boulder.boulder.dto.ProgramaSondajeDTO;
import com.boulder.boulder.entities.ProgramaSondaje;
import com.boulder.boulder.entities.Proyecto;
import com.boulder.boulder.repositories.ProgramaSondajeRepository;
import com.boulder.boulder.repositories.ProyectoRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * Implementación de los servicios de negocio para la gestión de programas de sondaje.
 *
 * <p>
 * Valida la consistencia de los alcances de perforación y realiza el mapeo plano 
 * desarmando las relaciones Lazy para el consumo fluido de Angular.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Service
public class ProgramaSondajeServiceImpl implements ProgramaSondajeService {

    private static final Logger log = LoggerFactory.getLogger(ProgramaSondajeServiceImpl.class);

    @Autowired
    private ProgramaSondajeRepository programaSondajeRepository;

    @Autowired
    private ProyectoRepository proyectoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProgramaSondajeDTO> listarTodos() {
        log.debug("Consultando el consolidado nacional de programas de sondaje");
        return programaSondajeRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProgramaSondajeDTO obtenerPorId(UUID id) {
        log.debug("Buscando programa de sondaje por ID: {}", id);
        ProgramaSondaje programa = programaSondajeRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Programa de sondaje no encontrado con ID: {}", id);
                    return new EntityNotFoundException("Programa de sondaje no localizado con el ID provisto.");
                });
        return convertToDto(programa);
    }

    @Override
    @Transactional(readOnly = true)
    public ProgramaSondajeDTO obtenerPorCodigo(String codigo) {
        log.debug("Buscando campaña de sondaje por código único: {}", codigo);

        ProgramaSondaje programa = programaSondajeRepository.findByCodigo(codigo)
                .orElseThrow(() -> {
                    log.warn("Programa de sondaje no encontrado con código: {}", codigo);
                    return new EntityNotFoundException("No se encontró ningún programa con el código: " + codigo);
                });
        return convertToDto(programa);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProgramaSondajeDTO> listarPorProyecto(UUID proyectoId) {
        log.debug("Filtrando planes de perforación para el proyecto ID: {}", proyectoId);

        return programaSondajeRepository.findByProyectoId(proyectoId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProgramaSondajeDTO> listarPorPlataforma(UUID proyectoId) {
        log.debug("Filtrando planes de perforación para plataforma ID: {}", proyectoId);

        return programaSondajeRepository.findByProyectoId(proyectoId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

   @Override
    @Transactional
    public ProgramaSondajeDTO guardar(ProgramaSondajeDTO dto) {
        log.info("Planificando nuevo programa de sondaje. Código: {}, Proyecto ID: {}", dto.getCodigo(), dto.getProyectoId());
        try {
            Proyecto proyecto = proyectoRepository.findById(dto.getProyectoId())
                    .orElseThrow(() -> new EntityNotFoundException("No se puede iniciar un programa si el Proyecto asignado no existe."));

            ProgramaSondaje programa = new ProgramaSondaje();
            programa.setCodigo(dto.getCodigo());
            programa.setNombre(dto.getNombre());
            programa.setProyecto(proyecto);
            
            programa.setEste(dto.getEste());
            programa.setNorte(dto.getNorte());
            programa.setElevacion(dto.getElevacion());
            programa.setProfundidad(dto.getProfundidad());
            programa.setAzimut(dto.getAzimut());
            programa.setInclinacion(dto.getInclinacion());
            programa.setPlano(dto.getPlano());
            programa.setPunto(dto.getPunto());
            programa.setPrioridad(dto.getPrioridad());
            programa.setZona(dto.getZona());
            programa.setFlanco(dto.getFlanco());
            programa.setNotas(dto.getNotas());

            ProgramaSondaje guardado = programaSondajeRepository.save(programa);
            log.info("Programa de sondaje registrado correctamente. ID: {}, Código: {}", guardado.getId(), guardado.getCodigo());
            return convertToDto(guardado);
        } catch (Exception e) {
            log.error("Fallo crítico al guardar el plan de sondajes con código: {}", dto.getCodigo(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public ProgramaSondajeDTO actualizar(UUID id, ProgramaSondajeDTO dto) {
        log.info("Modificando alcances del programa de sondaje ID: {}", id);
        try {
            ProgramaSondaje programa = programaSondajeRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Intento de modificar plan de sondaje inexistente ID: {}", id);
                        return new EntityNotFoundException("Programa de sondaje no existente.");
                    });

            if (dto.getProyectoId() != null && (programa.getProyecto() == null || !programa.getProyecto().getId().equals(dto.getProyectoId()))) {
                Proyecto nuevoProyecto = proyectoRepository.findById(dto.getProyectoId())
                        .orElseThrow(() -> new EntityNotFoundException("El nuevo Proyecto asociado no existe."));
                programa.setProyecto(nuevoProyecto);
            }

            programa.setNombre(dto.getNombre());
            programa.setEste(dto.getEste());
            programa.setNorte(dto.getNorte());
            programa.setElevacion(dto.getElevacion());
            programa.setProfundidad(dto.getProfundidad());
            programa.setAzimut(dto.getAzimut());
            programa.setInclinacion(dto.getInclinacion());
            programa.setPlano(dto.getPlano());
            programa.setPunto(dto.getPunto());
            programa.setPrioridad(dto.getPrioridad());
            programa.setZona(dto.getZona());
            programa.setFlanco(dto.getFlanco());
            programa.setNotas(dto.getNotas());

            ProgramaSondaje actualizado = programaSondajeRepository.save(programa);
            log.info("Programa de sondaje '{}' actualizado con éxito en terreno", actualizado.getCodigo());
            return convertToDto(actualizado);
        } catch (Exception e) {
            log.error("Error al modificar registros de ingeniería de sondajes ID: {}", id, e);
            throw e;
        }
    }

    private ProgramaSondajeDTO convertToDto(ProgramaSondaje programa) {
        ProgramaSondajeDTO dto = new ProgramaSondajeDTO();
        dto.setId(programa.getId());
        dto.setNumeroInterno(programa.getNumeroInterno());
        dto.setCodigo(programa.getCodigo());
        dto.setNombre(programa.getNombre());
        dto.setEste(programa.getEste());
        dto.setNorte(programa.getNorte());
        dto.setElevacion(programa.getElevacion());
        dto.setProfundidad(programa.getProfundidad());
        dto.setAzimut(programa.getAzimut());
        dto.setInclinacion(programa.getInclinacion());
        dto.setPlano(programa.getPlano());
        dto.setPunto(programa.getPunto());
        dto.setPrioridad(programa.getPrioridad());
        dto.setZona(programa.getZona());
        dto.setFlanco(programa.getFlanco());
        dto.setNotas(programa.getNotas());

        if (programa.getProyecto() != null) {
            dto.setProyectoId(programa.getProyecto().getId());
        }
        
        if (programa.getPlataforma() != null) {
            dto.setPlataformaId(programa.getPlataforma().getId());
        }
        
        return dto;
    }
}