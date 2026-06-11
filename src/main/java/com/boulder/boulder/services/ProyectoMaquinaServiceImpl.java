package com.boulder.boulder.services;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boulder.boulder.dto.ProyectoMaquinaDTO;
import com.boulder.boulder.entities.Maquina;
import com.boulder.boulder.entities.Proyecto;
import com.boulder.boulder.entities.ProyectoMaquina;
import com.boulder.boulder.repositories.MaquinaRepository;
import com.boulder.boulder.repositories.ProyectoMaquinaRepository;
import com.boulder.boulder.repositories.ProyectoRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * Implementación de los servicios de negocio para el control logístico de maquinaria pesada.
 *
 * <p>
 * Coordina el flujo de asignación resguardando la integridad referencial de los maestros 
 * involucrados y alterando dinámicamente el estado operativo de los equipos.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Service
public class ProyectoMaquinaServiceImpl implements ProyectoMaquinaService {

    private static final Logger log = LoggerFactory.getLogger(ProyectoMaquinaServiceImpl.class);

    @Autowired
    private ProyectoMaquinaRepository proyectoMaquinaRepository;

    @Autowired
    private ProyectoRepository proyectoRepository;

    @Autowired
    private MaquinaRepository maquinaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProyectoMaquinaDTO> listarTodas() {
        log.debug("Consultando historial completo de asignaciones logísticas de flota");
        return proyectoMaquinaRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProyectoMaquinaDTO obtenerPorId(UUID id) {
        log.debug("Buscando registro de asignación por ID: {}", id);
        ProyectoMaquina pm = proyectoMaquinaRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Registro de asignación no localizado con ID: {}", id);
                    return new EntityNotFoundException("Asignación de maquinaria no encontrada.");
                });
        return convertToDto(pm);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProyectoMaquinaDTO> listarPorProyecto(UUID proyectoId) {
        log.debug("Filtrando maquinaria asignada al proyecto ID: {}", proyectoId);
    
        return proyectoMaquinaRepository.findByProyectoId(proyectoId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProyectoMaquinaDTO guardar(ProyectoMaquinaDTO dto) {
        log.info("Registrando despliegue de Máquina ID: {} hacia Proyecto ID: {}", dto.getMaquinaId(), dto.getProyectoId());
        try {
  
            Proyecto proyecto = proyectoRepository.findById(dto.getProyectoId())
                    .orElseThrow(() -> new EntityNotFoundException("El Proyecto de destino especificado no existe."));

            Maquina maquina = maquinaRepository.findById(dto.getMaquinaId())
                    .orElseThrow(() -> new EntityNotFoundException("La Máquina seleccionada no existe en el catálogo."));

            ProyectoMaquina pm = new ProyectoMaquina();
            pm.setProyecto(proyecto);
            pm.setMaquina(maquina);
            pm.setFechaIngreso(dto.getFechaIngreso() != null ? dto.getFechaIngreso() : LocalDate.now());
            pm.setFechaSalida(dto.getFechaSalida());
            pm.setEstado(dto.getEstado() != null ? dto.getEstado() : "OPERANDO");

            if ("OPERANDO".equalsIgnoreCase(pm.getEstado())) {
                maquina.setEstado("ASIGNADO");
                maquinaRepository.save(maquina);
            }

            ProyectoMaquina guardado = proyectoMaquinaRepository.save(pm);
            log.info("Despliegue consolidado con éxito. ID de asignación: {}", guardado.getId());
            return convertToDto(guardado);
        } catch (Exception e) {
            log.error("Fallo logístico al registrar asignación de máquina", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public ProyectoMaquinaDTO actualizar(UUID id, ProyectoMaquinaDTO dto) {
        log.info("Actualizando condiciones operativas de la asignación ID: {}", id);
        try {
            ProyectoMaquina pm = proyectoMaquinaRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Intento de modificar asignación inexistente con ID: {}", id);
                        return new EntityNotFoundException("Registro de asignación operacional no encontrado.");
                    });

            pm.setFechaIngreso(dto.getFechaIngreso());
            pm.setFechaSalida(dto.getFechaSalida());
            pm.setEstado(dto.getEstado());

            if (pm.getFechaSalida() != null && pm.getMaquina() != null) {
                pm.setEstado("FINALIZADO");
                Maquina maquina = pm.getMaquina();
                maquina.setEstado("DISPONIBLE");
                maquinaRepository.save(maquina);
                log.info("Maquinaria {} desmovilizada de terreno. Estado restablecido a DISPONIBLE.", maquina.getCodigo());
            }

            ProyectoMaquina actualizado = proyectoMaquinaRepository.save(pm);
            log.info("Registro de control de flota ID: {} modificado correctamente", actualizado.getId());
            return convertToDto(actualizado);
        } catch (Exception e) {
            log.error("Error al actualizar la asignación de maquinaria ID: {}", id, e);
            throw e;
        }
    }

    private ProyectoMaquinaDTO convertToDto(ProyectoMaquina pm) {
        ProyectoMaquinaDTO dto = new ProyectoMaquinaDTO();
        dto.setId(pm.getId());
        dto.setNumeroInterno(pm.getNumeroInterno());
        dto.setFechaIngreso(pm.getFechaIngreso());
        dto.setFechaSalida(pm.getFechaSalida());
        dto.setEstado(pm.getEstado());

        if (pm.getProyecto() != null) {
            dto.setProyectoId(pm.getProyecto().getId());
        }

        if (pm.getMaquina() != null) {
            dto.setMaquinaId(pm.getMaquina().getId());
        }
        return dto;
    }
}