package com.boulder.boulder.services;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boulder.boulder.dto.RolDTO;
import com.boulder.boulder.entities.Rol;
import com.boulder.boulder.repositories.RolRepository;

/**
 * Implementación de los servicios de negocio para la gestión de roles.
 *
 * <p>
 * Gestiona las operaciones relacionadas con la administración de roles y
 * permisos de acceso.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Service
public class RolServiceImpl implements RolService {

    private static final Logger log = LoggerFactory.getLogger(RolServiceImpl.class);

    @Autowired
    private RolRepository rolRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RolDTO> listarTodos() {
        log.debug("Consultando el catálogo de roles del sistema");
        return rolRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private RolDTO convertToDto(Rol rol) {
        RolDTO dto = new RolDTO();
        dto.setId(rol.getId());
        dto.setNumeroInterno(rol.getNumeroInterno());
        dto.setCodigo(rol.getCodigo());
        dto.setNombre(rol.getNombre());
        return dto;
    }
}
