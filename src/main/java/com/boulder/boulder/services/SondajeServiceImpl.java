package com.boulder.boulder.services;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boulder.boulder.dto.SondajeDTO;
import com.boulder.boulder.entities.Sondaje;
import com.boulder.boulder.repositories.CollarRepository;
import com.boulder.boulder.repositories.MaquinaRepository;
import com.boulder.boulder.repositories.PlataformaRepository;
import com.boulder.boulder.repositories.ProgramaSondajeRepository;
import com.boulder.boulder.repositories.SondajeRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class SondajeServiceImpl implements SondajeService {

    @Autowired private SondajeRepository sondajeRepository;
    @Autowired private MaquinaRepository maquinaRepository;
    @Autowired private PlataformaRepository plataformaRepository;
    @Autowired private CollarRepository collarRepository;
    @Autowired private ProgramaSondajeRepository programaSondajeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SondajeDTO> listarPorProyecto(UUID proyectoId) {
        return sondajeRepository.findByProyectoId(proyectoId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SondajeDTO obtenerPorCodigo(String codigo) {
        Sondaje sondaje = sondajeRepository.findByCodigo(codigo)
                .orElseThrow(() -> new EntityNotFoundException("Sondaje no encontrado"));
        return convertToDto(sondaje);
    }

    @Override
    @Transactional
    public SondajeDTO guardar(SondajeDTO dto) {
        Sondaje sondaje = new Sondaje();
        sondaje.setProyectoId(dto.getProyectoId());
        sondaje.setCodigo(dto.getCodigo());
        sondaje.setNombre(dto.getNombre());
        sondaje.setAzimutSetup(dto.getAzimutSetup());
        sondaje.setInclinacionSetup(dto.getInclinacionSetup());
        sondaje.setProfundidadActual(0.0); // Comienza en cero metros

        // Mapeo seguro de llaves foráneas desde los IDs del DTO
        if (dto.getMaquinaId() != null) {
            sondaje.setMaquina(maquinaRepository.findById(dto.getMaquinaId()).orElse(null));
        }
        if (dto.getPlataformaId() != null) {
            sondaje.setPlataforma(plataformaRepository.findById(dto.getPlataformaId()).orElse(null));
        }
        if (dto.getCollarId() != null) {
            sondaje.setCollar(collarRepository.findById(dto.getCollarId()).orElse(null));
        }
        if (dto.getProgramaSondajeId() != null) {
            sondaje.setProgramaSondaje(programaSondajeRepository.findById(dto.getProgramaSondajeId()).orElse(null));
        }

        return convertToDto(sondajeRepository.save(sondaje));
    }

    @Override
    @Transactional
    public SondajeDTO actualizarAvance(UUID id, Double nuevaProfundidad) {
        Sondaje sondaje = sondajeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sondaje no encontrado"));
        
        // Regla de negocio: La nueva profundidad no puede ser menor a la que ya teníamos medida
        if (nuevaProfundidad < sondaje.getProfundidadActual()) {
            throw new IllegalArgumentException("Error: La nueva profundidad no puede ser menor al avance actual registrado.");
        }
        
        sondaje.setProfundidadActual(nuevaProfundidad);
        return convertToDto(sondajeRepository.save(sondaje));
    }

    // Aplanamiento de Entidad a DTO para evitar LazyInitializationException
    private SondajeDTO convertToDto(Sondaje s) {
        SondajeDTO dto = new SondajeDTO();
        dto.setId(s.getId());
        dto.setCodigo(s.getCodigo());
        dto.setNombre(s.getNombre());
        dto.setProyectoId(s.getProyectoId());
        dto.setAzimutSetup(s.getAzimutSetup());
        dto.setInclinacionSetup(s.getInclinacionSetup());
        dto.setProfundidadActual(s.getProfundidadActual());
        dto.setProfundidadFinal(s.getProfundidadFinal());
        dto.setFechaInicio(s.getFechaInicio());
        dto.setFechaFin(s.getFechaFin());

        if (s.getProgramaSondaje() != null) {
            dto.setProgramaSondajeId(s.getProgramaSondaje().getId());
            dto.setProgramaSondajeNombre(s.getProgramaSondaje().getNombre());
        }
        if (s.getCollar() != null) {
            dto.setCollarId(s.getCollar().getId());
            dto.setCollarCodigo(s.getCollar().getCodigo());
        }
        if (s.getPlataforma() != null) {
            dto.setPlataformaId(s.getPlataforma().getId());
            dto.setPlataformaCodigo(s.getPlataforma().getCodigo());
        }
        if (s.getMaquina() != null) {
            dto.setMaquinaId(s.getMaquina().getId());
            dto.setMaquinaNombre(s.getMaquina().getNombre());
        }
        return dto;
    }
}