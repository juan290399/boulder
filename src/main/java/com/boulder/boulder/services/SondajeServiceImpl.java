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

import com.boulder.boulder.dto.SondajeDTO;
import com.boulder.boulder.entities.Sondaje;
import com.boulder.boulder.entities.Proyecto;
import com.boulder.boulder.entities.ProgramaSondaje;
import com.boulder.boulder.entities.Collar;
import com.boulder.boulder.entities.Plataforma;
import com.boulder.boulder.entities.Maquina;

import com.boulder.boulder.repositories.SondajeRepository;
import com.boulder.boulder.repositories.ProyectoRepository;
import com.boulder.boulder.repositories.ProgramaSondajeRepository;
import com.boulder.boulder.repositories.CollarRepository;
import com.boulder.boulder.repositories.PlataformaRepository;
import com.boulder.boulder.repositories.MaquinaRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * Implementación de los servicios de negocio para la gestión operativa de Sondajes.
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Service
public class SondajeServiceImpl implements SondajeService {

    private static final Logger log = LoggerFactory.getLogger(SondajeServiceImpl.class);

    @Autowired
    private SondajeRepository sondajeRepository;
    @Autowired
    private ProyectoRepository proyectoRepository;
    @Autowired
    private ProgramaSondajeRepository programaSondajeRepository;
    @Autowired
    private CollarRepository collarRepository;
    @Autowired
    private PlataformaRepository plataformaRepository;
    @Autowired
    private MaquinaRepository maquinaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SondajeDTO> listarTodos() {
        log.debug("Consultando el registro general de sondajes operativos");
        return sondajeRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SondajeDTO obtenerPorId(UUID id) {
        log.debug("Buscando registro de sondaje por ID único: {}", id);
        Sondaje sondaje = sondajeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Registro de sondaje no localizado con el ID provisto."));
        return convertToDto(sondaje);
    }

    @Override
    @Transactional(readOnly = true)
    public SondajeDTO obtenerPorCodigo(String codigo) {
        log.debug("Buscando ejecución de sondaje por código único: {}", codigo);
        Sondaje sondaje = sondajeRepository.findByCodigo(codigo)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró ningún registro de sondaje con el código: " + codigo));
        return convertToDto(sondaje);
    }

    @Override
    @Transactional(readOnly = true)
    public SondajeDTO obtenerPorCollar(UUID collarId) {
        log.debug("Buscando el sondaje asociado de forma única al Collar ID: {}", collarId);
        return sondajeRepository.findByCollarId(collarId)
                .map(this::convertToDto)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "No se encontró ninguna ejecución de sondaje para el Collar ID especificado."));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SondajeDTO> listarPorProyecto(UUID proyectoId) {
        log.debug("Consultando el histórico completo de sondajes asignados al Proyecto ID: {}", proyectoId);
        return sondajeRepository.findByProyectoId(proyectoId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SondajeDTO> listarPorPlataforma(UUID plataformaId) {
        log.debug("Filtrando ejecuciones de sondaje en la Plataforma ID: {}", plataformaId);
        return sondajeRepository.findByPlataformaId(plataformaId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SondajeDTO> listarPorMaquina(UUID maquinaId) {
        log.debug("Filtrando sondajes perforados por la Máquina/Rig ID: {}", maquinaId);
        return sondajeRepository.findByMaquinaId(maquinaId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SondajeDTO guardar(SondajeDTO dto) {
        log.info("Registrando ejecución física de un nuevo Sondaje. Código: {}", dto.getCodigo());
        try {
            validar(dto, true);
            // El código identifica al sondaje en partes, plantillas y reportes:
            // se guarda sin espacios y en mayúsculas para que no haya dos que
            // solo se diferencien en eso.
            String codigo = dto.getCodigo().trim().toUpperCase(Locale.ROOT);
            if (sondajeRepository.existsByCodigo(codigo)) {
                throw new IllegalArgumentException("Ya existe un sondaje con el código " + codigo + ".");
            }

            Sondaje sondaje = new Sondaje();
            sondaje.setCodigo(codigo);
            sondaje.setNombre(dto.getNombre().trim());
            sondaje.setAzimutSetup(dto.getAzimutSetup());
            sondaje.setInclinacionSetup(dto.getInclinacionSetup());
            sondaje.setProfundidadActual(dto.getProfundidadActual());
            sondaje.setProfundidadFinal(dto.getProfundidadFinal());
            sondaje.setMedicionUtilizada(dto.getMedicionUtilizada());
            sondaje.setMedicionesTotales(dto.getMedicionesTotales());
            sondaje.setFechaInicio(dto.getFechaInicio());
            sondaje.setFechaFin(dto.getFechaFin());
            sondaje.setCampania(dto.getCampania());
            sondaje.setNotas(dto.getNotas());

            if (dto.getProyectoId() != null) {
                Proyecto p = proyectoRepository.findById(dto.getProyectoId())
                        .orElseThrow(() -> new EntityNotFoundException("El Proyecto asignado no existe."));
                sondaje.setProyecto(p);
            }
            if (dto.getProgramaSondajeId() != null) {
                ProgramaSondaje ps = programaSondajeRepository.findById(dto.getProgramaSondajeId())
                        .orElseThrow(() -> new EntityNotFoundException("El Programa de Sondaje asignado no existe."));
                sondaje.setProgramaSondaje(ps);
            }
            if (dto.getCollarId() != null) {
                Collar c = collarRepository.findById(dto.getCollarId())
                        .orElseThrow(() -> new EntityNotFoundException("El Collar asignado no existe."));
                sondaje.setCollar(c);
            }
            if (dto.getPlataformaId() != null) {
                Plataforma pl = plataformaRepository.findById(dto.getPlataformaId())
                        .orElseThrow(() -> new EntityNotFoundException("La Plataforma asignada no existe."));
                sondaje.setPlataforma(pl);
            }
            if (dto.getMaquinaId() != null) {
                Maquina m = maquinaRepository.findById(dto.getMaquinaId())
                        .orElseThrow(() -> new EntityNotFoundException("La Máquina asignada no existe."));
                sondaje.setMaquina(m);
            }

            Sondaje guardado = sondajeRepository.save(sondaje);
            log.info("Sondaje guardado con éxito. ID: {}, Código: {}", guardado.getId(), guardado.getCodigo());
            return convertToDto(guardado);
        } catch (Exception e) {
            log.error("Fallo crítico al registrar el sondaje código: {}", dto.getCodigo(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public SondajeDTO actualizar(UUID id, SondajeDTO dto) {
        log.info("Actualizando parámetros del Sondaje ID: {}", id);
        try {
            Sondaje sondaje = sondajeRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Sondaje no existente en la base de datos."));

            validar(dto, false);
            sondaje.setNombre(dto.getNombre().trim());
            sondaje.setAzimutSetup(dto.getAzimutSetup());
            sondaje.setInclinacionSetup(dto.getInclinacionSetup());
            sondaje.setProfundidadActual(dto.getProfundidadActual());
            sondaje.setProfundidadFinal(dto.getProfundidadFinal());
            sondaje.setMedicionUtilizada(dto.getMedicionUtilizada());
            sondaje.setMedicionesTotales(dto.getMedicionesTotales());
            sondaje.setFechaInicio(dto.getFechaInicio());
            sondaje.setFechaFin(dto.getFechaFin());
            sondaje.setCampania(dto.getCampania());
            sondaje.setNotas(dto.getNotas());

            if (dto.getProyectoId() != null && (sondaje.getProyecto() == null || !sondaje.getProyecto().getId().equals(dto.getProyectoId()))) {
                sondaje.setProyecto(proyectoRepository.findById(dto.getProyectoId())
                        .orElseThrow(() -> new EntityNotFoundException("El nuevo Proyecto no existe.")));
            }
            if (dto.getProgramaSondajeId() != null && (sondaje.getProgramaSondaje() == null || !sondaje.getProgramaSondaje().getId().equals(dto.getProgramaSondajeId()))) {
                sondaje.setProgramaSondaje(programaSondajeRepository.findById(dto.getProgramaSondajeId())
                        .orElseThrow(() -> new EntityNotFoundException("El nuevo Programa de Sondaje no existe.")));
            }
            if (dto.getCollarId() != null && (sondaje.getCollar() == null || !sondaje.getCollar().getId().equals(dto.getCollarId()))) {
                sondaje.setCollar(collarRepository.findById(dto.getCollarId())
                        .orElseThrow(() -> new EntityNotFoundException("El nuevo Collar no existe.")));
            }
            // Plataforma y máquina son opcionales: sin id, el sondaje queda sin
            // ellas (el formulario ofrece «Sin plataforma» y «Sin máquina»).
            if (dto.getPlataformaId() == null) {
                sondaje.setPlataforma(null);
            } else if (sondaje.getPlataforma() == null || !dto.getPlataformaId().equals(sondaje.getPlataforma().getId())) {
                sondaje.setPlataforma(plataformaRepository.findById(dto.getPlataformaId())
                        .orElseThrow(() -> new EntityNotFoundException("La nueva Plataforma no existe.")));
            }
            if (dto.getMaquinaId() == null) {
                sondaje.setMaquina(null);
            } else if (sondaje.getMaquina() == null || !dto.getMaquinaId().equals(sondaje.getMaquina().getId())) {
                sondaje.setMaquina(maquinaRepository.findById(dto.getMaquinaId())
                        .orElseThrow(() -> new EntityNotFoundException("La nueva Máquina no existe.")));
            }

            Sondaje actualizado = sondajeRepository.save(sondaje);
            log.info("Sondaje '{}' actualizado con éxito en terreno", actualizado.getCodigo());
            return convertToDto(actualizado);
        } catch (Exception e) {
            log.error("Error al modificar el sondaje ID: {}", id, e);
            throw e;
        }
    }

    /**
     * Reglas que la base no puede explicar: sin esto, un alta incompleta
     * llegaba como «Falta un dato obligatorio» sin decir cuál. Se juntan todos
     * los problemas en un solo mensaje para corregirlos de una vez.
     */
    private static void validar(SondajeDTO dto, boolean esAlta) {
        List<String> problemas = new ArrayList<>();
        if (esAlta) {
            if (enBlanco(dto.getCodigo())) {
                problemas.add("falta el código");
            } else if (dto.getCodigo().trim().length() > 25) {
                problemas.add("el código no puede superar los 25 caracteres");
            }
            if (dto.getProyectoId() == null) {
                problemas.add("falta el proyecto");
            }
            if (dto.getCollarId() == null) {
                problemas.add("falta el collar");
            }
        }
        if (enBlanco(dto.getNombre())) {
            problemas.add("falta el nombre corto");
        } else if (dto.getNombre().trim().length() > 25) {
            problemas.add("el nombre corto no puede superar los 25 caracteres");
        }
        if (dto.getAzimutSetup() != null && (dto.getAzimutSetup() < 0 || dto.getAzimutSetup() > 360)) {
            problemas.add("el azimut va de 0° a 360°");
        }
        if (dto.getInclinacionSetup() != null && (dto.getInclinacionSetup() < -90 || dto.getInclinacionSetup() > 90)) {
            problemas.add("la inclinación va de -90° a 90°");
        }
        if (dto.getProfundidadActual() != null && dto.getProfundidadActual() < 0) {
            problemas.add("la profundidad actual no puede ser negativa");
        }
        if (dto.getProfundidadFinal() != null && dto.getProfundidadFinal() <= 0) {
            problemas.add("la profundidad programada debe ser mayor que cero");
        }
        if (dto.getFechaInicio() != null && dto.getFechaFin() != null && dto.getFechaFin().isBefore(dto.getFechaInicio())) {
            problemas.add("la fecha de fin no puede ser anterior a la de inicio");
        }
        if (!problemas.isEmpty()) {
            String texto = String.join("; ", problemas);
            throw new IllegalArgumentException(Character.toUpperCase(texto.charAt(0)) + texto.substring(1) + ".");
        }
    }

    private static boolean enBlanco(String valor) {
        return valor == null || valor.isBlank();
    }

    private SondajeDTO convertToDto(Sondaje sondaje) {
        SondajeDTO dto = new SondajeDTO();
        dto.setId(sondaje.getId());
        dto.setNumeroInterno(sondaje.getNumeroInterno());
        dto.setCodigo(sondaje.getCodigo());
        dto.setNombre(sondaje.getNombre());
        dto.setAzimutSetup(sondaje.getAzimutSetup());
        dto.setInclinacionSetup(sondaje.getInclinacionSetup());
        dto.setProfundidadActual(sondaje.getProfundidadActual());
        dto.setProfundidadFinal(sondaje.getProfundidadFinal());
        dto.setMedicionUtilizada(sondaje.getMedicionUtilizada());
        dto.setMedicionesTotales(sondaje.getMedicionesTotales());
        dto.setFechaInicio(sondaje.getFechaInicio());
        dto.setFechaFin(sondaje.getFechaFin());
        dto.setCampania(sondaje.getCampania());
        dto.setNotas(sondaje.getNotas());

        if (sondaje.getProyecto() != null) {
            dto.setProyectoId(sondaje.getProyecto().getId());
        }
        if (sondaje.getProgramaSondaje() != null) {
            dto.setProgramaSondajeId(sondaje.getProgramaSondaje().getId());
        }
        if (sondaje.getCollar() != null) {
            dto.setCollarId(sondaje.getCollar().getId());
        }
        if (sondaje.getPlataforma() != null) {
            dto.setPlataformaId(sondaje.getPlataforma().getId());
        }
        if (sondaje.getMaquina() != null) {
            dto.setMaquinaId(sondaje.getMaquina().getId());
        }

        return dto;
    }
}