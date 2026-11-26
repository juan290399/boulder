package com.boulder.boulder.services;

import java.util.ArrayList;
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
            validar(dto);
            String codigo = dto.getCodigo().trim();
            String ruc = dto.getRuc().trim();
            if (empresaRepository.existsByCodigo(codigo)) {
                throw new IllegalArgumentException("Ya existe una empresa con el código " + codigo + ".");
            }
            if (empresaRepository.findByRuc(ruc).isPresent()) {
                throw new IllegalArgumentException("Ya existe una empresa con el RUC " + ruc + ".");
            }

            Empresa empresa = new Empresa();
            copiar(dto, empresa);

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

            validar(dto);
            String codigo = dto.getCodigo().trim();
            String ruc = dto.getRuc().trim();
            if (!codigo.equals(empresa.getCodigo()) && empresaRepository.existsByCodigo(codigo)) {
                throw new IllegalArgumentException("Ya existe una empresa con el código " + codigo + ".");
            }
            empresaRepository.findByRuc(ruc)
                    .filter(otra -> !otra.getId().equals(id))
                    .ifPresent(otra -> {
                        throw new IllegalArgumentException("Ya existe una empresa con el RUC " + ruc + ".");
                    });
            copiar(dto, empresa);

            Empresa actualizada = empresaRepository.save(empresa);
            log.info("Empresa '{}' actualizada con éxito", actualizada.getNombre());
            return convertToDto(actualizada);
        } catch (Exception e) {
            log.error("Error al modificar registros de empresa ID: {}", id, e);
            throw e;
        }
    }

    /** Código, razón social y RUC (11 dígitos) son obligatorios; el correo, si viene, con formato válido. */
    private static void validar(EmpresaDTO dto) {
        List<String> problemas = new ArrayList<>();
        if (enBlanco(dto.getCodigo())) {
            problemas.add("el código es obligatorio");
        }
        if (enBlanco(dto.getNombre())) {
            problemas.add("la razón social es obligatoria");
        }
        if (enBlanco(dto.getRuc())) {
            problemas.add("el RUC es obligatorio");
        } else if (!dto.getRuc().trim().matches("\\d{11}")) {
            problemas.add("el RUC debe tener 11 dígitos");
        }
        if (!enBlanco(dto.getCorreo()) && !dto.getCorreo().trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            problemas.add("el correo no tiene un formato válido");
        }
        if (!problemas.isEmpty()) {
            String texto = String.join("; ", problemas);
            throw new IllegalArgumentException(Character.toUpperCase(texto.charAt(0)) + texto.substring(1) + ".");
        }
    }

    /** Pasa los campos editables del DTO a la entidad; los opcionales vacíos quedan en null. */
    private static void copiar(EmpresaDTO dto, Empresa empresa) {
        empresa.setCodigo(dto.getCodigo().trim());
        empresa.setNombre(dto.getNombre().trim());
        empresa.setRuc(dto.getRuc().trim());
        empresa.setDireccion(enBlanco(dto.getDireccion()) ? null : dto.getDireccion().trim());
        empresa.setTelefono(enBlanco(dto.getTelefono()) ? null : dto.getTelefono().trim());
        empresa.setCorreo(enBlanco(dto.getCorreo()) ? null : dto.getCorreo().trim());
    }

    private static boolean enBlanco(String valor) {
        return valor == null || valor.trim().isEmpty();
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
