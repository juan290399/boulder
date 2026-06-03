package com.boulder.boulder.services;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boulder.boulder.dto.MaquinaDTO;
import com.boulder.boulder.entities.Maquina;
import com.boulder.boulder.repositories.MaquinaRepository;

import jakarta.persistence.EntityNotFoundException;
@Service
public class MaquinaServiceImpl implements MaquinaService {

    private static final Logger log =
        LoggerFactory.getLogger(MaquinaServiceImpl.class);

    @Autowired
    private MaquinaRepository maquinaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MaquinaDTO> listarTodas() {

        log.debug("Consultando listado de máquinas");

        List<MaquinaDTO> maquinas = maquinaRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        log.info("Se encontraron {} máquinas", maquinas.size());

        return maquinas;
    }

    @Override
    @Transactional(readOnly = true)
    public MaquinaDTO obtenerPorId(UUID id) {

        log.debug("Buscando máquina con ID: {}", id);

        Maquina maquina = maquinaRepository.findById(id)
                .orElseThrow(() -> {

                    log.warn("Máquina no encontrada con ID: {}", id);

                    return new EntityNotFoundException(
                            "Máquina no encontrada con ID: " + id);
                });

        log.info("Máquina encontrada: {}", maquina.getCodigo());

        return convertToDto(maquina);
    }

    @Override
    @Transactional(readOnly = true)
    public MaquinaDTO obtenerPorCodigo(String codigo) {

        log.debug("Buscando máquina con código: {}", codigo);

        Maquina maquina = maquinaRepository.findByCodigo(codigo)
                .orElseThrow(() -> {

                    log.warn("Máquina no encontrada con código: {}", codigo);

                    return new EntityNotFoundException(
                            "Máquina no encontrada con código: " + codigo);
                });

        log.info("Máquina encontrada con código {}", codigo);

        return convertToDto(maquina);
    }

    @Override
    @Transactional
    public MaquinaDTO guardar(MaquinaDTO dto) {

        log.info(
            "Creando máquina. Código: {}, Nombre: {}, Tipo: {}",
            dto.getCodigo(),
            dto.getNombre(),
            dto.getTipo()
        );

        try {

            Maquina maquina = new Maquina();

            maquina.setCodigo(dto.getCodigo());
            maquina.setNombre(dto.getNombre());
            maquina.setTipo(dto.getTipo());
            maquina.setMarca(dto.getMarca());
            maquina.setEstado(
                    dto.getEstado() != null
                            ? dto.getEstado()
                            : "DISPONIBLE");

            Maquina guardada = maquinaRepository.save(maquina);

            log.info(
                "Máquina creada correctamente. ID: {}, Código: {}",
                guardada.getId(),
                guardada.getCodigo()
            );

            return convertToDto(guardada);

        } catch (Exception e) {

            log.error(
                "Error al crear máquina con código {}",
                dto.getCodigo(),
                e
            );

            throw e;
        }
    }

    @Override
    @Transactional
    public MaquinaDTO actualizar(UUID id, MaquinaDTO dto) {

        log.info("Actualizando máquina con ID {}", id);

        try {

            Maquina maquina = maquinaRepository.findById(id)
                    .orElseThrow(() -> {

                        log.warn(
                            "Intento de actualizar máquina inexistente {}",
                            id
                        );

                        return new EntityNotFoundException(
                                "Máquina no encontrada");
                    });

            maquina.setNombre(dto.getNombre());
            maquina.setTipo(dto.getTipo());
            maquina.setMarca(dto.getMarca());
            maquina.setEstado(dto.getEstado());

            Maquina actualizada = maquinaRepository.save(maquina);

            log.info(
                "Máquina {} actualizada correctamente",
                actualizada.getCodigo()
            );

            return convertToDto(actualizada);

        } catch (Exception e) {

            log.error(
                "Error actualizando máquina {}",
                id,
                e
            );

            throw e;
        }
    }

    private MaquinaDTO convertToDto(Maquina maquina) {
        MaquinaDTO dto = new MaquinaDTO();
        dto.setId(maquina.getId());
        dto.setCodigo(maquina.getCodigo());
        dto.setNombre(maquina.getNombre());
        dto.setTipo(maquina.getTipo());
        dto.setMarca(maquina.getMarca());
        dto.setEstado(maquina.getEstado());
        return dto;
    }
}