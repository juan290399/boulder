package com.boulder.boulder.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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

/**
 * Implementación de los servicios de negocio para la gestión de máquinas.
 *
 * <p>
 * Gestiona las operaciones de consulta, registro y actualización de máquinas
 * utilizadas en los proyectos de exploración.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
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
        log.info("Creando máquina. Código: {}, Nombre: {}", dto.getCodigo(), dto.getNombre());
        try {
            validar(dto, true);
            String codigo = dto.getCodigo().trim().toUpperCase(Locale.ROOT);
            if (maquinaRepository.existsByCodigo(codigo)) {
                throw new IllegalArgumentException("Ya existe una máquina con el código " + codigo + ".");
            }
            Maquina maquina = new Maquina();
            maquina.setCodigo(codigo);
            maquina.setEstado(limpio(dto.getEstado()) != null ? dto.getEstado().trim() : "Operativa");
            copiar(dto, maquina);

            Maquina guardada = maquinaRepository.save(maquina);
            log.info("Máquina creada correctamente. ID: {}, Código: {}", guardada.getId(), guardada.getCodigo());
            return convertToDto(guardada);
        } catch (Exception e) {
            log.error("Error al crear máquina con código {}", dto.getCodigo(), e);
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
                        log.warn("Intento de actualizar máquina inexistente {}", id);
                        return new EntityNotFoundException("Máquina no encontrada");
                    });

            validar(dto, false);
            if (limpio(dto.getEstado()) != null) {
                maquina.setEstado(dto.getEstado().trim());
            }
            // Antes se perdían el motor y los alcances editados.
            copiar(dto, maquina);

            Maquina actualizada = maquinaRepository.save(maquina);
            log.info("Máquina {} actualizada correctamente", actualizada.getCodigo());
            return convertToDto(actualizada);
        } catch (Exception e) {
            log.error("Error actualizando máquina {}", id, e);
            throw e;
        }
    }

    /** Campos que el formulario edita, iguales en el alta y en la edición. */
    private static void copiar(MaquinaDTO dto, Maquina maquina) {
        maquina.setNombre(dto.getNombre().trim());
        maquina.setTipo(limpio(dto.getTipo()));
        maquina.setMarca(limpio(dto.getMarca()));
        maquina.setMotor(limpio(dto.getMotor()));
        maquina.setAlcancePq(dto.getAlcancePq());
        maquina.setAlcanceHq(dto.getAlcanceHq());
        maquina.setAlcanceNq(dto.getAlcanceNq());
        maquina.setFechaCompra(dto.getFechaCompra());
        maquina.setFechaLlegada(dto.getFechaLlegada());
        maquina.setFechaInicio(dto.getFechaInicio());
        maquina.setNotas(limpio(dto.getNotas()));
    }

    private static void validar(MaquinaDTO dto, boolean esAlta) {
        List<String> problemas = new ArrayList<>();
        if (esAlta) {
            if (enBlanco(dto.getCodigo())) {
                problemas.add("falta el código");
            } else if (dto.getCodigo().trim().length() > 10) {
                problemas.add("el código no puede superar los 10 caracteres");
            }
        }
        if (enBlanco(dto.getNombre())) {
            problemas.add("falta el nombre");
        } else if (dto.getNombre().trim().length() > 100) {
            problemas.add("el nombre no puede superar los 100 caracteres");
        }
        for (Short alcance : new Short[] { dto.getAlcancePq(), dto.getAlcanceHq(), dto.getAlcanceNq() }) {
            if (alcance != null && alcance < 0) {
                problemas.add("un alcance no puede ser negativo");
                break;
            }
        }
        lanzarSiHay(problemas);
    }

    private static void lanzarSiHay(List<String> problemas) {
        if (!problemas.isEmpty()) {
            String texto = String.join("; ", problemas);
            throw new IllegalArgumentException(Character.toUpperCase(texto.charAt(0)) + texto.substring(1) + ".");
        }
    }

    private static boolean enBlanco(String valor) {
        return valor == null || valor.isBlank();
    }

    private static String limpio(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private MaquinaDTO convertToDto(Maquina maquina) {
        MaquinaDTO dto = new MaquinaDTO();
        dto.setId(maquina.getId());
        dto.setCodigo(maquina.getCodigo());
        dto.setNombre(maquina.getNombre());
        dto.setTipo(maquina.getTipo());
        dto.setMarca(maquina.getMarca());
        dto.setEstado(maquina.getEstado());
        dto.setNumeroInterno(maquina.getNumeroInterno());
        dto.setMotor(maquina.getMotor());
        dto.setAlcancePq(maquina.getAlcancePq());
        dto.setAlcanceHq(maquina.getAlcanceHq());
        dto.setAlcanceNq(maquina.getAlcanceNq());
        dto.setFechaCompra(maquina.getFechaCompra());
        dto.setFechaLlegada(maquina.getFechaLlegada());
        dto.setFechaInicio(maquina.getFechaInicio());
        dto.setNotas(maquina.getNotas());
        return dto;
    }
}