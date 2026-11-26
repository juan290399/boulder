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

import com.boulder.boulder.dto.CargoDTO;
import com.boulder.boulder.dto.EmpleadoDTO;
import com.boulder.boulder.entities.Cargo;
import com.boulder.boulder.entities.Empleado;
import com.boulder.boulder.entities.Empresa;
import com.boulder.boulder.repositories.CargoRepository;
import com.boulder.boulder.repositories.EmpleadoRepository;
import com.boulder.boulder.repositories.EmpresaRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * Implementación de los servicios de negocio para el catálogo de personal.
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Service
public class EmpleadoServiceImpl implements EmpleadoService {

    private static final Logger log = LoggerFactory.getLogger(EmpleadoServiceImpl.class);

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private CargoRepository cargoRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EmpleadoDTO> listarTodos() {
        log.debug("Consultando el catálogo completo de personal");
        return empleadoRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmpleadoDTO> listarPorArea(String area) {
        log.debug("Consultando personal activo del área: {}", area);
        return empleadoRepository.findByCargoAreaAndEstadoTrue(area).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EmpleadoDTO obtenerPorId(UUID id) {
        log.debug("Buscando empleado por ID: {}", id);
        return convertToDto(buscar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public EmpleadoDTO obtenerPorCodigo(String codigo) {
        log.debug("Buscando empleado por código: {}", codigo);
        Empleado empleado = empleadoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new EntityNotFoundException("Empleado no encontrado con código: " + codigo));
        return convertToDto(empleado);
    }

    @Override
    @Transactional
    public EmpleadoDTO guardar(EmpleadoDTO dto) {
        log.info("Registrando empleado. Código: {}", dto.getCodigo());
        validar(dto);

        if (empleadoRepository.existsByCodigo(dto.getCodigo())) {
            throw new IllegalArgumentException(
                    "El código '" + dto.getCodigo() + "' ya está asignado a otro empleado.");
        }

        Empleado empleado = new Empleado();
        aplicar(dto, empleado);

        Empleado guardado = empleadoRepository.save(empleado);
        log.info("Empleado creado. ID: {}, Código: {}", guardado.getId(), guardado.getCodigo());
        return convertToDto(guardado);
    }

    @Override
    @Transactional
    public EmpleadoDTO actualizar(UUID id, EmpleadoDTO dto) {
        log.info("Actualizando empleado ID: {}", id);
        Empleado empleado = buscar(id);
        validar(dto);

        // El código identifica al empleado en los partes; si cambia, tiene que
        // seguir siendo único.
        if (!empleado.getCodigo().equals(dto.getCodigo())
                && empleadoRepository.existsByCodigo(dto.getCodigo())) {
            throw new IllegalArgumentException(
                    "El código '" + dto.getCodigo() + "' ya está asignado a otro empleado.");
        }

        aplicar(dto, empleado);
        return convertToDto(empleadoRepository.save(empleado));
    }

    @Override
    @Transactional
    public void cambiarEstado(UUID id, boolean activo) {
        log.warn("Cambiando estado del empleado ID: {} a -> {}", id, activo);
        Empleado empleado = buscar(id);
        empleado.setEstado(activo);
        empleadoRepository.save(empleado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CargoDTO> listarCargos() {
        log.debug("Consultando el catálogo de cargos");
        return cargoRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Lo que {@code @Valid} no cubre: los largos de las columnas (un valor más
     * largo revienta en la base con un error genérico) y el orden de las fechas.
     */
    private static void validar(EmpleadoDTO dto) {
        List<String> problemas = new ArrayList<>();
        excede(problemas, dto.getCodigo(), 15, "el código");
        excede(problemas, dto.getNombres(), 80, "los nombres");
        excede(problemas, dto.getApellidos(), 80, "los apellidos");
        excede(problemas, dto.getDocumento(), 20, "el documento");
        excede(problemas, dto.getEmail(), 120, "el correo");
        excede(problemas, dto.getTelefono(), 30, "el teléfono");
        if (dto.getFechaIngreso() != null && dto.getFechaSalida() != null
                && dto.getFechaSalida().isBefore(dto.getFechaIngreso())) {
            problemas.add("la fecha de salida no puede ser anterior a la de ingreso");
        }
        if (!problemas.isEmpty()) {
            String texto = String.join("; ", problemas);
            throw new IllegalArgumentException(Character.toUpperCase(texto.charAt(0)) + texto.substring(1) + ".");
        }
    }

    private static void excede(List<String> problemas, String valor, int maximo, String campo) {
        if (valor != null && valor.trim().length() > maximo) {
            problemas.add(campo + " no puede superar los " + maximo + " caracteres");
        }
    }

    private Empleado buscar(UUID id) {
        return empleadoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Empleado no encontrado con ID: " + id));
    }

    private void aplicar(EmpleadoDTO dto, Empleado empleado) {
        empleado.setCodigo(dto.getCodigo());
        empleado.setNombres(dto.getNombres());
        empleado.setApellidos(dto.getApellidos());
        empleado.setDocumento(dto.getDocumento());
        empleado.setEmail(dto.getEmail());
        empleado.setTelefono(dto.getTelefono());
        empleado.setFechaIngreso(dto.getFechaIngreso());
        empleado.setFechaSalida(dto.getFechaSalida());
        empleado.setEstado(dto.getEstado() != null ? dto.getEstado() : Boolean.TRUE);
        empleado.setNotas(dto.getNotas());

        if (dto.getCargoId() != null) {
            Cargo cargo = cargoRepository.findById(dto.getCargoId())
                    .orElseThrow(() -> new EntityNotFoundException("El cargo asignado no existe."));
            empleado.setCargo(cargo);
        } else {
            empleado.setCargo(null);
        }

        if (dto.getEmpresaId() != null) {
            Empresa empresa = empresaRepository.findById(dto.getEmpresaId())
                    .orElseThrow(() -> new EntityNotFoundException("La empresa asignada no existe."));
            empleado.setEmpresa(empresa);
        } else {
            empleado.setEmpresa(null);
        }
    }

    private EmpleadoDTO convertToDto(Empleado empleado) {
        EmpleadoDTO dto = new EmpleadoDTO();
        dto.setId(empleado.getId());
        dto.setNumeroInterno(empleado.getNumeroInterno());
        dto.setCodigo(empleado.getCodigo());
        dto.setNombres(empleado.getNombres());
        dto.setApellidos(empleado.getApellidos());
        dto.setDocumento(empleado.getDocumento());
        dto.setEmail(empleado.getEmail());
        dto.setTelefono(empleado.getTelefono());
        dto.setFechaIngreso(empleado.getFechaIngreso());
        dto.setFechaSalida(empleado.getFechaSalida());
        dto.setEstado(empleado.getEstado());
        dto.setNotas(empleado.getNotas());

        if (empleado.getEmpresa() != null) {
            dto.setEmpresaId(empleado.getEmpresa().getId());
        }

        if (empleado.getCargo() != null) {
            dto.setCargoId(empleado.getCargo().getId());
            dto.setCargoNombre(empleado.getCargo().getNombre());
            dto.setCargoArea(empleado.getCargo().getArea());
        }

        return dto;
    }

    private CargoDTO convertToDto(Cargo cargo) {
        CargoDTO dto = new CargoDTO();
        dto.setId(cargo.getId());
        dto.setNumeroInterno(cargo.getNumeroInterno());
        dto.setCodigo(cargo.getCodigo());
        dto.setNombre(cargo.getNombre());
        dto.setArea(cargo.getArea());
        dto.setEstado(cargo.getEstado());
        return dto;
    }
}
