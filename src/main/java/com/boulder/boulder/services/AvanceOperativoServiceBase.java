package com.boulder.boulder.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import com.boulder.boulder.dto.AvanceOperativoDTO;
import com.boulder.boulder.entities.AvanceOperativo;
import com.boulder.boulder.eventos.CambioDatos;
import com.boulder.boulder.repositories.AvanceOperativoRepository;
import com.boulder.boulder.repositories.SondajeRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * Lógica compartida por los siete servicios de partes de avance operativo.
 *
 * <p>
 * Todo lo que es idéntico entre UC16 y UC20 —el CRUD, la resolución del
 * sondaje, el volcado de la cabecera y el estado por defecto— vive aquí. Cada
 * servicio concreto solo aporta cuatro piezas: su repositorio, cómo fabricar
 * una entidad y un DTO vacíos, y el mapeo de los campos que le son propios.
 * </p>
 *
 * <p>
 * La alternativa era repetir unas ciento cincuenta líneas siete veces. Con eso
 * cualquier corrección de la cabecera —por ejemplo, admitir el código suelto
 * cuando no hay relación— habría que aplicarla en siete sitios, y basta
 * olvidarse de uno para que un parte se comporte distinto de sus hermanos sin
 * que nada lo delate.
 * </p>
 *
 * @param <E> entidad concreta del parte
 * @param <D> DTO concreto del parte
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
public abstract class AvanceOperativoServiceBase<E extends AvanceOperativo, D extends AvanceOperativoDTO>
        implements AvanceOperativoService<D> {

    private static final Logger log = LoggerFactory.getLogger(AvanceOperativoServiceBase.class);

    @Autowired
    private SondajeRepository sondajeRepository;

    @Autowired
    private ApplicationEventPublisher eventos;

    // ---------------------------------------------------------------- ganchos

    /** Repositorio del parte concreto. */
    protected abstract AvanceOperativoRepository<E> repositorio();

    /** Instancia vacía de la entidad, para las altas. */
    protected abstract E nuevaEntidad();

    /** Instancia vacía del DTO, para las lecturas. */
    protected abstract D nuevoDto();

    /** Nombre legible del parte; solo se usa en logs y mensajes de error. */
    protected abstract String nombreParte();

    /** Copia del DTO a la entidad los campos propios del parte. */
    protected abstract void aplicarDetalle(D dto, E entidad);

    /** Copia de la entidad al DTO los campos propios del parte. */
    protected abstract void volcarDetalle(E entidad, D dto);

    // ------------------------------------------------------------------ CRUD

    @Override
    @Transactional(readOnly = true)
    public List<D> listarTodos() {
        log.debug("Consultando el listado de partes de {}", nombreParte());
        return repositorio().findAll().stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public D obtenerPorId(UUID id) {
        log.debug("Buscando parte de {} ID: {}", nombreParte(), id);
        return convertToDto(buscar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<D> listarPorSondaje(UUID sondajeId) {
        log.debug("Consultando partes de {} del sondaje ID: {}", nombreParte(), sondajeId);
        return repositorio().findBySondajeIdOrderByFechaAscNumeroInternoAsc(sondajeId).stream()
                .map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<D> listarPorFecha(LocalDate fecha) {
        log.debug("Consultando partes de {} de la fecha: {}", nombreParte(), fecha);
        return repositorio().findByFechaOrderByNumeroInternoAsc(fecha).stream()
                .map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<D> listarPorRango(LocalDate desde, LocalDate hasta) {
        log.debug("Consultando partes de {} entre {} y {}", nombreParte(), desde, hasta);
        return repositorio().findByFechaBetweenOrderByFechaAscNumeroInternoAsc(desde, hasta).stream()
                .map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public D guardar(D dto) {
        log.info("Registrando parte de {}. Sondaje: {}, Fecha: {}", nombreParte(),
                dto.getSondajeCodigo() != null ? dto.getSondajeCodigo() : dto.getSondajeId(), dto.getFecha());
        E entidad = nuevaEntidad();
        aplicarCabecera(dto, entidad);
        aplicarDetalle(dto, entidad);
        E guardado = repositorio().save(entidad);
        log.info("Parte de {} registrado. ID: {}", nombreParte(), guardado.getId());
        avisar(guardado);
        return convertToDto(guardado);
    }

    @Override
    @Transactional
    public D actualizar(UUID id, D dto) {
        log.info("Actualizando parte de {} ID: {}", nombreParte(), id);
        E entidad = buscar(id);
        aplicarCabecera(dto, entidad);
        aplicarDetalle(dto, entidad);
        E guardado = repositorio().save(entidad);
        avisar(guardado);
        return convertToDto(guardado);
    }

    @Override
    @Transactional
    public void eliminar(UUID id) {
        log.warn("Eliminando parte de {} ID: {}", nombreParte(), id);
        E entidad = buscar(id);
        repositorio().delete(entidad);
        avisar(entidad);
    }

    // ------------------------------------------------------------- auxiliares

    /** Avisa a las pantallas abiertas que cambió el frente de este sondaje. */
    private void avisar(E entidad) {
        Set<java.util.UUID> sondajes = entidad.getSondaje() != null
                ? Set.of(entidad.getSondaje().getId()) : Set.of();
        eventos.publishEvent(CambioDatos.de("avance", sondajes, "web", 1));
    }

    private E buscar(UUID id) {
        return repositorio().findById(id).orElseThrow(
                () -> new EntityNotFoundException("Parte de " + nombreParte() + " no encontrado con ID: " + id));
    }

    /**
     * Vuelca la cabecera del DTO sobre la entidad y resuelve el sondaje.
     *
     * <p>
     * Exigir uno de los dos identificadores no es una comodidad: es el mismo
     * {@code CHECK} que impone la tabla. Si se deja pasar un parte sin ninguno,
     * el error llega desde PostgreSQL como una violación de restricción, sin
     * decir qué campo falta.
     * </p>
     */
    private void aplicarCabecera(D dto, E entidad) {
        if (dto.getSondajeId() == null && (dto.getSondajeCodigo() == null || dto.getSondajeCodigo().isBlank())) {
            throw new IllegalArgumentException("Debe indicar el sondaje por ID o por código.");
        }

        entidad.setSondajeCodigo(dto.getSondajeCodigo());
        entidad.setFecha(dto.getFecha());
        entidad.setTurno(dto.getTurno());
        entidad.setEstado(dto.getEstado() != null ? dto.getEstado() : "registrado");
        entidad.setObservaciones(dto.getObservaciones());

        entidad.setSondaje(dto.getSondajeId() == null ? null
                : sondajeRepository.findById(dto.getSondajeId())
                        .orElseThrow(() -> new EntityNotFoundException("El Sondaje asignado no existe.")));
    }

    /**
     * Construye el DTO completo a partir de la entidad.
     *
     * <p>
     * Además del código tal como se tecleó, se expone el código canónico del
     * sondaje relacionado. Las dos cosas no coinciden: en la base conviven
     * {@code DDH-35}, {@code Sondaje 036} y {@code VERO S 38_26} para sondajes
     * cuyo código real es {@code DDH-VERO-S-0NN-26}. La pantalla necesita el
     * canónico para agrupar sin que el usuario pierda de vista lo que se
     * escribió en el parte de papel.
     * </p>
     */
    private D convertToDto(E entidad) {
        D dto = nuevoDto();
        dto.setId(entidad.getId());
        dto.setNumeroInterno(entidad.getNumeroInterno());
        dto.setSondajeCodigo(entidad.getSondajeCodigo());
        dto.setFecha(entidad.getFecha());
        dto.setTurno(entidad.getTurno());
        dto.setEstado(entidad.getEstado());
        dto.setObservaciones(entidad.getObservaciones());

        if (entidad.getSondaje() != null) {
            dto.setSondajeId(entidad.getSondaje().getId());
            dto.setSondajeCodigoReal(entidad.getSondaje().getCodigo());
        }

        volcarDetalle(entidad, dto);
        return dto;
    }
}
