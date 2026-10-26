package com.boulder.boulder.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boulder.boulder.dominio.ParteTurno;
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

    private static final Set<String> TURNOS = Set.of("dia", "noche");

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
        validar(dto, null);
        AvancePerforacion avance = new AvancePerforacion();
        aplicarCabecera(dto, avance);
        reemplazarPersonal(dto, avance);
        AvancePerforacion guardado = avanceRepository.save(avance);
        // El INSERT se manda ya, no al cerrar la transacción: así vuelve el
        // número interno que asigna la secuencia (@Generated) en la respuesta.
        avanceRepository.flush();
        log.info("Parte de avance registrado. ID: {}", guardado.getId());
        return convertToDto(guardado);
    }

    @Override
    @Transactional
    public AvancePerforacionDTO actualizar(UUID id, AvancePerforacionDTO dto) {
        log.info("Actualizando parte de avance ID: {}", id);
        AvancePerforacion avance = buscar(id);
        validar(dto, id);
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
        Sondaje sondaje = sondajeRepository.findById(sondajeId)
                .orElseThrow(() -> new EntityNotFoundException("Sondaje no encontrado con ID: " + sondajeId));

        AvancePerforacionDTO dto = new AvancePerforacionDTO();
        dto.setSondajeId(sondajeId);
        dto.setSondajeCodigo(sondaje.getCodigo());
        dto.setDesde(BigDecimal.ZERO);
        Maquina maquina = sondaje.getMaquina();
        Plataforma plataforma = sondaje.getPlataforma();

        // El turno siguiente arranca donde el anterior dejó el pozo, casi siempre
        // con la misma máquina y la misma cuadrilla: se precarga y se corrige lo
        // que cambió, en vez de teclear todo de nuevo.
        AvancePerforacion ultimo = avanceRepository.findFirstBySondajeIdOrderByHastaDescFechaDesc(sondajeId)
                .orElse(null);
        if (ultimo != null) {
            dto.setDesde(ultimo.getHasta());
            dto.setResponsable(ultimo.getResponsable());
            dto.setLinea(ultimo.getLinea());
            maquina = ultimo.getMaquina() != null ? ultimo.getMaquina() : maquina;
            plataforma = ultimo.getPlataforma() != null ? ultimo.getPlataforma() : plataforma;
            dto.setPersonal(ultimo.getPersonal().stream().map(p -> {
                AvancePersonalDTO pdto = new AvancePersonalDTO();
                pdto.setNombre(p.getNombre());
                pdto.setCargo(p.getCargo());
                pdto.setCodigoMaquina(p.getCodigoMaquina());
                if (p.getPerforista() != null) {
                    pdto.setPerforistaId(p.getPerforista().getId());
                }
                return pdto;
            }).collect(Collectors.toList()));
        }
        if (maquina != null) {
            dto.setMaquinaId(maquina.getId());
            dto.setMaquinaCodigo(maquina.getCodigo());
        }
        if (plataforma != null) {
            dto.setPlataformaId(plataforma.getId());
            dto.setPlataformaCodigo(plataforma.getCodigo());
        }
        return dto;
    }

    /**
     * Rechaza el parte antes de tocar nada: números que no cuadran, un turno que
     * no existe o el mismo turno cargado dos veces (lo que pasa cuando el parte
     * llega por WhatsApp y además alguien lo teclea).
     *
     * @param idActual el parte que se corrige, para no compararlo consigo mismo; {@code null} al crear
     */
    private void validar(AvancePerforacionDTO dto, UUID idActual) {
        List<String> errores = new ArrayList<>(ParteTurno.errores(dto.getDesde(), dto.getHasta(), dto.getAvance(),
                dto.getRetornoPct(), dto.getCasingDesde(), dto.getCasingHasta()));
        if (dto.getTurno() != null && !TURNOS.contains(dto.getTurno())) {
            errores.add("turno: tiene que ser dia o noche");
        }
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join("; ", errores));
        }
        if (dto.getSondajeId() == null) {
            return;
        }
        boolean repetido = avanceRepository.findBySondajeIdAndFecha(dto.getSondajeId(), dto.getFecha()).stream()
                .filter(a -> !Objects.equals(a.getId(), idActual))
                .anyMatch(a -> Objects.equals(a.getTurno(), dto.getTurno())
                        && a.getDesde().compareTo(dto.getDesde()) == 0);
        if (repetido) {
            throw new IllegalStateException("Ese turno ya está cargado: el sondaje tiene un parte del "
                    + dto.getFecha() + (dto.getTurno() == null ? "" : ", turno " + dto.getTurno())
                    + ", que arranca en " + dto.getDesde().toPlainString() + " m.");
        }
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

        // Con la relación resuelta, el código de texto sale de la entidad: el
        // formulario manda ids y los listados y el reporte leen el código.
        if (avance.getSondaje() != null) {
            avance.setSondajeCodigo(avance.getSondaje().getCodigo());
        }
        if (avance.getMaquina() != null) {
            avance.setMaquinaCodigo(avance.getMaquina().getCodigo());
        }
        if (avance.getPlataforma() != null) {
            avance.setPlataformaCodigo(avance.getPlataforma().getCodigo());
        }
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
