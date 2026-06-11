package com.boulder.boulder.services;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boulder.boulder.dto.EmpresaDTO;
import com.boulder.boulder.entities.Empresa;
import com.boulder.boulder.repositories.EmpresaRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * Implementación de los servicios de negocio para la gestión de empresas.
 *
 * <p>
 * Gestiona las operaciones de consulta, registro y actualización de empresas
 * mediante el acceso a la capa de persistencia.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Service
public class EmpresaServiceImpl implements EmpresaService{
    private static final Logger log = LoggerFactory.getLogger(EmpresaServiceImpl.class);

    @Autowired
    private EmpresaRepository empresaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EmpresaDTO> listarTodas() {
        log.debug("Consultando el catálogo general de empresas registradas");
        return empresaRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EmpresaDTO obtenerPorId(UUID id) {
        log.debug("Buscando empresa por ID único: {}", id);
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Empresa no localizada con el ID provisto."));
        return convertToDto(empresa);
    }

    @Override
    @Transactional(readOnly = true)
    public EmpresaDTO obtenerPorRuc(String ruc) {
        log.debug("Buscando empresa por registro fiscal/RUC: {}", ruc);
        Empresa empresa = empresaRepository.findByRuc(ruc)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró ninguna empresa con el RUC: " + ruc));
        return convertToDto(empresa);
    }

    @Override
    @Transactional
    public EmpresaDTO guardar(EmpresaDTO dto) {
        log.info("Insertando nueva empresa en el sistema: {}", dto.getNombre());
        try {
            Empresa empresa = new Empresa();
            empresa.setRuc(dto.getRuc());
            empresa.setNombre(dto.getNombre());
            empresa.setDireccion(dto.getDireccion());
            empresa.setTelefono(dto.getTelefono());

            Empresa guardada = empresaRepository.save(empresa);
            log.info("Empresa registrada exitosamente con ID: {}", guardada.getId());
            return convertToDto(guardada);
        } catch (Exception e) {
            log.error("Fallo al registrar empresa: {}", dto.getNombre(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public EmpresaDTO actualizar(UUID id, EmpresaDTO dto) {
        log.info("Actualizando metadatos de la empresa con ID: {}", id);
        try {
            Empresa empresa = empresaRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Empresa no existente."));

            empresa.setNombre(dto.getNombre());
            empresa.setDireccion(dto.getDireccion());
            empresa.setTelefono(dto.getTelefono());

            Empresa actualizada = empresaRepository.save(empresa);
            log.info("Empresa '{}' actualizada con éxito", actualizada.getNombre());
            return convertToDto(actualizada);
        } catch (Exception e) {
            log.error("Error al modificar registros de empresa ID: {}", id, e);
            throw e;
        }
    }

    private EmpresaDTO convertToDto(Empresa empresa) {
        EmpresaDTO dto = new EmpresaDTO();
        dto.setId(empresa.getId());
        dto.setNumeroInterno(empresa.getNumeroInterno());
        dto.setCodigo(empresa.getCodigo());
        dto.setNombre(empresa.getNombre());
        dto.setRuc(empresa.getRuc());
        dto.setDireccion(empresa.getDireccion());
        dto.setTelefono(empresa.getTelefono());
        dto.setCorreo(empresa.getCorreo());
        return dto;
    }
}
