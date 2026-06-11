package com.boulder.boulder.services;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boulder.boulder.dto.ProyectoDTO;
import com.boulder.boulder.entities.Empresa;
import com.boulder.boulder.entities.Proyecto;
import com.boulder.boulder.repositories.EmpresaRepository;
import com.boulder.boulder.repositories.ProyectoRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * Implementación de los servicios de negocio para la gestión de proyectos.
 *
 * <p>
 * Gestiona las operaciones de consulta, registro y actualización de proyectos,
 * asegurando la integridad referencial con la empresa mandante asociada.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Service
public class ProyectoServiceImpl implements ProyectoService {

    private static final Logger log = LoggerFactory.getLogger(ProyectoServiceImpl.class);

    @Autowired
    private ProyectoRepository proyectoRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProyectoDTO> listarTodos() {
        log.debug("Consultando el catálogo general de proyectos activos");
        return proyectoRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProyectoDTO obtenerPorId(UUID id) {
        log.debug("Buscando proyecto por ID único: {}", id);
        Proyecto proyecto = proyectoRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Proyecto no encontrado con ID: {}", id);
                    return new EntityNotFoundException("Proyecto no localizado con el ID provisto.");
                });
        return convertToDto(proyecto);
    }

    @Override
    @Transactional(readOnly = true)
    public ProyectoDTO obtenerPorCodigo(String codigo) {
        log.debug("Buscando proyecto por código: {}", codigo);
        Proyecto proyecto = proyectoRepository.findByCodigo(codigo)
                .orElseThrow(() -> {
                    log.warn("Proyecto no encontrado con código: {}", codigo);
                    return new EntityNotFoundException("No se encontró ningún proyecto con el código: " + codigo);
                });
        return convertToDto(proyecto);
    }

    @Override
    @Transactional
    public ProyectoDTO guardar(ProyectoDTO dto) {
        log.info("Insertando nuevo proyecto en el sistema: {}, Código: {}", dto.getNombre(), dto.getCodigo());
        try {
            Empresa empresa = empresaRepository.findById(dto.getEmpresaId())
                    .orElseThrow(() -> new EntityNotFoundException("La Empresa asociada con ID " + dto.getEmpresaId() + " no existe."));

            Proyecto proyecto = new Proyecto();
            proyecto.setCodigo(dto.getCodigo());
            proyecto.setNombre(dto.getNombre());
            proyecto.setUbicacion(dto.getUbicacion());
            proyecto.setSrid(dto.getSrid());
            proyecto.setEmpresa(empresa);

            Proyecto guardado = proyectoRepository.save(proyecto);
            log.info("Proyecto registrado exitosamente con ID: {}, Código: {}", guardado.getId(), guardado.getCodigo());
            return convertToDto(guardado);
        } catch (Exception e) {
            log.error("Fallo al registrar proyecto: {}", dto.getNombre(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public ProyectoDTO actualizar(UUID id, ProyectoDTO dto) {
        log.info("Actualizando metadatos del proyecto con ID: {}", id);
        try {
            Proyecto proyecto = proyectoRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Intento de actualizar proyecto inexistente con ID: {}", id);
                        return new EntityNotFoundException("Proyecto no existente.");
                    });

            
            if (dto.getEmpresaId() != null && !proyecto.getEmpresa().getId().equals(dto.getEmpresaId())) {
                Empresa nuevaEmpresa = empresaRepository.findById(dto.getEmpresaId())
                        .orElseThrow(() -> new EntityNotFoundException("La nueva Empresa asociada no existe."));
                proyecto.setEmpresa(nuevaEmpresa);
            }

            proyecto.setNombre(dto.getNombre());
            proyecto.setUbicacion(dto.getUbicacion());
            proyecto.setSrid(dto.getSrid());

            Proyecto actualizado = proyectoRepository.save(proyecto);
            log.info("Proyecto '{}' actualizado con éxito", actualizado.getCodigo());
            return convertToDto(actualizado);
        } catch (Exception e) {
            log.error("Error al modificar registros de proyecto ID: {}", id, e);
            throw e;
        }
    }

    private ProyectoDTO convertToDto(Proyecto proyecto) {
        ProyectoDTO dto = new ProyectoDTO();
        dto.setId(proyecto.getId());
        dto.setNumeroInterno(proyecto.getNumeroInterno());
        dto.setCodigo(proyecto.getCodigo());
        dto.setNombre(proyecto.getNombre());
        dto.setUbicacion(proyecto.getUbicacion());
        dto.setSrid(proyecto.getSrid());
        
        if (proyecto.getEmpresa() != null) {
            dto.setEmpresaId(proyecto.getEmpresa().getId());
        }
        return dto;
    }
}