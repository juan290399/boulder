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
            validar(dto, true);
            // Código sin espacios y en mayúsculas: lo cruzan partes y plantillas.
            String codigo = dto.getCodigo().trim().toUpperCase(Locale.ROOT);
            if (plataformaRepository.existsByCodigo(codigo)) {
                throw new IllegalArgumentException("Ya existe una plataforma con el código " + codigo + ".");
            }
            Proyecto proyecto = proyectoRepository.findById(dto.getProyectoId())
                    .orElseThrow(() -> new EntityNotFoundException("El Proyecto asociado con ID " + dto.getProyectoId() + " no existe."));

            Plataforma plataforma = new Plataforma();
            plataforma.setCodigo(codigo);
            plataforma.setProyecto(proyecto);
            plataforma.setEstado(limpio(dto.getEstado()) != null ? dto.getEstado().trim() : "En construccion");
            copiar(dto, plataforma);

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

            validar(dto, false);
            if (dto.getProyectoId() != null
                    && (plataforma.getProyecto() == null || !dto.getProyectoId().equals(plataforma.getProyecto().getId()))) {
                Proyecto nuevoProyecto = proyectoRepository.findById(dto.getProyectoId())
                        .orElseThrow(() -> new EntityNotFoundException("El nuevo Proyecto asociado no existe."));
                plataforma.setProyecto(nuevoProyecto);
            }
            if (limpio(dto.getEstado()) != null) {
                plataforma.setEstado(dto.getEstado().trim());
            }
            // Antes solo se guardaban el nombre y el estado: las coordenadas,
            // el tipo, la ubicación y las pozas editadas se perdían sin aviso.
            copiar(dto, plataforma);

            Plataforma actualizada = plataformaRepository.save(plataforma);
            log.info("Plataforma {} actualizada correctamente", actualizada.getCodigo());
            return convertToDto(actualizada);
        } catch (Exception e) {
            log.error("Error actualizando la plataforma con ID: {}", id, e);
            throw e;
        }
    }

    /** Campos que el formulario edita, iguales en el alta y en la edición. */
    private static void copiar(PlataformaDTO dto, Plataforma plataforma) {
        plataforma.setNombre(dto.getNombre().trim());
        plataforma.setEste(dto.getEste());
        plataforma.setNorte(dto.getNorte());
        plataforma.setElevacion(dto.getElevacion());
        plataforma.setEjeMayor(dto.getEjeMayor());
        plataforma.setEjeMinor(dto.getEjeMinor());
        plataforma.setEjeZ(dto.getEjeZ());
        plataforma.setArea(dto.getArea());
        plataforma.setNumeroPozas(dto.getNumeroPozas() != null ? dto.getNumeroPozas() : 0);
        plataforma.setTipo(limpio(dto.getTipo()));
        plataforma.setUbicacion(limpio(dto.getUbicacion()));
        plataforma.setNotas(limpio(dto.getNotas()));
    }

    /**
     * Lo que la base exige y no sabe explicar: sin esto el alta fallaba siempre
     * con «Falta un dato obligatorio» (las coordenadas no se copiaban).
     */
    private static void validar(PlataformaDTO dto, boolean esAlta) {
        List<String> problemas = new ArrayList<>();
        if (esAlta) {
            if (enBlanco(dto.getCodigo())) {
                problemas.add("falta el código");
            } else if (dto.getCodigo().trim().length() > 8) {
                problemas.add("el código no puede superar los 8 caracteres");
            }
            if (dto.getProyectoId() == null) {
                problemas.add("falta el proyecto");
            }
        }
        if (enBlanco(dto.getNombre())) {
            problemas.add("falta el nombre");
        } else if (dto.getNombre().trim().length() > 50) {
            problemas.add("el nombre no puede superar los 50 caracteres");
        }
        // Rango UTM del hemisferio sur (zonas 17S a 19S, las del Perú): fuera
        // de él, el punto no cae en la zona o se escribió en otra unidad.
        if (dto.getEste() == null) {
            problemas.add("falta la coordenada este");
        } else if (dto.getEste() < 160_000 || dto.getEste() > 840_000) {
            problemas.add("la coordenada este debe estar entre 160 000 y 840 000 m");
        }
        if (dto.getNorte() == null) {
            problemas.add("falta la coordenada norte");
        } else if (dto.getNorte() < 0 || dto.getNorte() > 10_000_000) {
            problemas.add("la coordenada norte debe estar entre 0 y 10 000 000 m");
        }
        if (dto.getElevacion() == null) {
            problemas.add("falta la elevación");
        }
        if (dto.getNumeroPozas() != null && dto.getNumeroPozas() < 0) {
            problemas.add("el número de pozas no puede ser negativo");
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

    private PlataformaDTO convertToDto(Plataforma plataforma) {
        PlataformaDTO dto = new PlataformaDTO();
        dto.setId(plataforma.getId());
        dto.setNumeroInterno(plataforma.getNumeroInterno());
        dto.setCodigo(plataforma.getCodigo());
        dto.setNombre(plataforma.getNombre());
        dto.setEstado(plataforma.getEstado());
        dto.setEste(plataforma.getEste());
        dto.setNorte(plataforma.getNorte());
        dto.setElevacion(plataforma.getElevacion());
        dto.setEjeMayor(plataforma.getEjeMayor());
        dto.setEjeMinor(plataforma.getEjeMinor());
        dto.setEjeZ(plataforma.getEjeZ());
        dto.setArea(plataforma.getArea());
        dto.setNumeroPozas(plataforma.getNumeroPozas());
        dto.setTipo(plataforma.getTipo());
        dto.setUbicacion(plataforma.getUbicacion());
        dto.setNotas(plataforma.getNotas());
        
        if (plataforma.getProyecto() != null) {
            dto.setProyectoId(plataforma.getProyecto().getId());
        }
        return dto;
    }
}