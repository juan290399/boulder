package com.boulder.boulder.services;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boulder.boulder.dto.AvancePerforacionDTO;
import com.boulder.boulder.dto.AvancePersonalDTO;
import com.boulder.boulder.entities.AvancePerforacion;
import com.boulder.boulder.entities.AvancePersonal;
import com.boulder.boulder.entities.Maquina;
import com.boulder.boulder.entities.Plataforma;
import com.boulder.boulder.entities.Sondaje;
import com.boulder.boulder.repositories.AvancePerforacionRepository;
import com.boulder.boulder.repositories.MaquinaRepository;
import com.boulder.boulder.repositories.EmpleadoRepository;
import com.boulder.boulder.repositories.PlataformaRepository;
import com.boulder.boulder.repositories.SondajeRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * Implementación de los servicios de negocio para los partes diarios de avance
 * de perforación.
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Service
public class AvancePerforacionServiceImpl implements AvancePerforacionService {

    private static final Logger log = LoggerFactory.getLogger(AvancePerforacionServiceImpl.class);

    private final AvancePerforacionRepository avanceRepository;
    private final SondajeRepository sondajeRepository;
    private final MaquinaRepository maquinaRepository;
    private final PlataformaRepository plataformaRepository;
    private final EmpleadoRepository empleadoRepository;

    public AvancePerforacionServiceImpl(AvancePerforacionRepository avanceRepository,
            SondajeRepository sondajeRepository, MaquinaRepository maquinaRepository,
            PlataformaRepository plataformaRepository, EmpleadoRepository empleadoRepository) {
        this.avanceRepository = avanceRepository;
        this.sondajeRepository = sondajeRepository;
        this.maquinaRepository = maquinaRepository;
        this.plataformaRepository = plataformaRepository;
        this.empleadoRepository = empleadoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvancePerforacionDTO> listarTodos() {
        log.debug("Consultando el listado de partes de avance de perforación");
        return avanceRepository.findAll().stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AvancePerforacionDTO obtenerPorId(UUID id) {
        log.debug("Buscando parte de avance de perforación ID: {}", id);
        return convertToDto(buscar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvancePerforacionDTO> listarPorSondaje(UUID sondajeId) {
        log.debug("Consultando partes de avance del sondaje ID: {}", sondajeId);
        return avanceRepository.findBySondajeIdOrderByFechaAscTurnoAsc(sondajeId).stream()
                .map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvancePerforacionDTO> listarPorFecha(LocalDate fecha) {
        log.debug("Consultando partes de avance de la fecha: {}", fecha);
        return avanceRepository.findByFecha(fecha).stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AvancePerforacionDTO guardar(AvancePerforacionDTO dto) {
        log.info("Registrando parte de avance. Sondaje: {}, Fecha: {}",
                dto.getSondajeCodigo() != null ? dto.getSondajeCodigo() : dto.getSondajeId(), dto.getFecha());
        AvancePerforacion avance = new AvancePerforacion();
        aplicarCabecera(dto, avance);
        reemplazarPersonal(dto, avance);
        AvancePerforacion guardado = avanceRepository.save(avance);
        log.info("Parte de avance registrado. ID: {}", guardado.getId());
        return convertToDto(guardado);
    }

    @Override
    @Transactional
    public AvancePerforacionDTO actualizar(UUID id, AvancePerforacionDTO dto) {
        log.info("Actualizando parte de avance ID: {}", id);
        AvancePerforacion avance = buscar(id);
        aplicarCabecera(dto, avance);
        reemplazarPersonal(dto, avance);
        return convertToDto(avanceRepository.save(avance));
    }

    @Override
    @Transactional
    public void eliminar(UUID id) {
        log.warn("Eliminando parte de avance ID: {}", id);
        avanceRepository.delete(buscar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public AvancePerforacionDTO sugerencia(UUID sondajeId) {
        return null;
    }

    private AvancePerforacion buscar(UUID id) {
        return avanceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Parte de avance de perforación no encontrado con ID: " + id));
    }

    private void aplicarCabecera(AvancePerforacionDTO dto, AvancePerforacion avance) {
        if (dto.getSondajeId() == null && (dto.getSondajeCodigo() == null || dto.getSondajeCodigo().isBlank())) {
            throw new IllegalArgumentException("Debe indicar el sondaje por ID o por código.");
        }

        avance.setSondajeCodigo(dto.getSondajeCodigo());
        avance.setFecha(dto.getFecha());
        avance.setTurno(dto.getTurno());
        avance.setResponsable(dto.getResponsable());
        avance.setMaquinaCodigo(dto.getMaquinaCodigo());
        avance.setPlataformaCodigo(dto.getPlataformaCodigo());
        avance.setDesde(dto.getDesde());
        avance.setHasta(dto.getHasta());
        avance.setAvance(dto.getAvance());
        avance.setCasingTipo(dto.getCasingTipo());
        avance.setCasingDesde(dto.getCasingDesde());
        avance.setCasingHasta(dto.getCasingHasta());
        avance.setRetornoPct(dto.getRetornoPct());
        avance.setLinea(dto.getLinea());
        avance.setComentarios(dto.getComentarios());
        avance.setEstado(dto.getEstado() != null ? dto.getEstado() : "registrado");
        avance.setObservaciones(dto.getObservaciones());

        avance.setSondaje(dto.getSondajeId() == null ? null
                : sondajeRepository.findById(dto.getSondajeId())
                        .orElseThrow(() -> new EntityNotFoundException("El Sondaje asignado no existe.")));
        avance.setMaquina(dto.getMaquinaId() == null ? null
                : maquinaRepository.findById(dto.getMaquinaId())
                        .orElseThrow(() -> new EntityNotFoundException("La Máquina asignada no existe.")));
        avance.setPlataforma(dto.getPlataformaId() == null ? null
                : plataformaRepository.findById(dto.getPlataformaId())
                        .orElseThrow(() -> new EntityNotFoundException("La Plataforma asignada no existe.")));
    }

    private void reemplazarPersonal(AvancePerforacionDTO dto, AvancePerforacion avance) {
        avance.getPersonal().clear();
        if (dto.getPersonal() == null) {
            return;
        }
        for (AvancePersonalDTO pdto : dto.getPersonal()) {
            AvancePersonal p = new AvancePersonal();
            p.setAvance(avance);
            p.setNombre(pdto.getNombre());
            p.setCargo(pdto.getCargo());
            p.setCodigoMaquina(pdto.getCodigoMaquina());
            if (pdto.getPerforistaId() != null) {
                p.setPerforista(empleadoRepository.findById(pdto.getPerforistaId())
                        .orElseThrow(() -> new EntityNotFoundException("El empleado asignado como perforista no existe.")));
            }
            avance.getPersonal().add(p);
        }
    }

    private AvancePerforacionDTO convertToDto(AvancePerforacion avance) {
        AvancePerforacionDTO dto = new AvancePerforacionDTO();
        dto.setId(avance.getId());
        dto.setNumeroInterno(avance.getNumeroInterno());
        dto.setSondajeCodigo(avance.getSondajeCodigo());
        dto.setFecha(avance.getFecha());
        dto.setTurno(avance.getTurno());
        dto.setResponsable(avance.getResponsable());
        dto.setMaquinaCodigo(avance.getMaquinaCodigo());
        dto.setPlataformaCodigo(avance.getPlataformaCodigo());
        dto.setDesde(avance.getDesde());
        dto.setHasta(avance.getHasta());
        dto.setAvance(avance.getAvance());
        dto.setCasingTipo(avance.getCasingTipo());
        dto.setCasingDesde(avance.getCasingDesde());
        dto.setCasingHasta(avance.getCasingHasta());
        dto.setRetornoPct(avance.getRetornoPct());
        dto.setLinea(avance.getLinea());
        dto.setComentarios(avance.getComentarios());
        dto.setEstado(avance.getEstado());
        dto.setObservaciones(avance.getObservaciones());

        if (avance.getSondaje() != null) {
            dto.setSondajeId(avance.getSondaje().getId());
        }
        if (avance.getMaquina() != null) {
            dto.setMaquinaId(avance.getMaquina().getId());
        }
        if (avance.getPlataforma() != null) {
            dto.setPlataformaId(avance.getPlataforma().getId());
        }

        dto.setPersonal(avance.getPersonal().stream().map(p -> {
            AvancePersonalDTO pdto = new AvancePersonalDTO();
            pdto.setId(p.getId());
            pdto.setNombre(p.getNombre());
            pdto.setCargo(p.getCargo());
            pdto.setCodigoMaquina(p.getCodigoMaquina());
            if (p.getPerforista() != null) {
                pdto.setPerforistaId(p.getPerforista().getId());
            }
            return pdto;
        }).collect(Collectors.toList()));

        return dto;
    }
}
