package com.boulder.boulder.services;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boulder.boulder.dto.MaquinaDTO;
import com.boulder.boulder.entities.Maquina;
import com.boulder.boulder.repositories.MaquinaRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class MaquinaServiceImpl implements MaquinaService {

    @Autowired
    private MaquinaRepository maquinaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MaquinaDTO> listarTodas() {
        return maquinaRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MaquinaDTO obtenerPorId(UUID id) {
        Maquina maquina = maquinaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Máquina no encontrada con ID: " + id));
        return convertToDto(maquina);
    }

    @Override
    @Transactional(readOnly = true)
    public MaquinaDTO obtenerPorCodigo(String codigo) {
        Maquina maquina = maquinaRepository.findByCodigo(codigo)
                .orElseThrow(() -> new EntityNotFoundException("Máquina no encontrada con código: " + codigo));
        return convertToDto(maquina);
    }

    @Override
    @Transactional
    public MaquinaDTO guardar(MaquinaDTO dto) {
        Maquina maquina = new Maquina();
        maquina.setCodigo(dto.getCodigo());
        maquina.setNombre(dto.getNombre());
        maquina.setTipo(dto.getTipo());
        maquina.setMarca(dto.getMarca());
        maquina.setEstado(dto.getEstado() != null ? dto.getEstado() : "DISPONIBLE");
        
        return convertToDto(maquinaRepository.save(maquina));
    }

    @Override
    @Transactional
    public MaquinaDTO actualizar(UUID id, MaquinaDTO dto) {
        Maquina maquina = maquinaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Máquina no encontrada"));
        
        maquina.setNombre(dto.getNombre());
        maquina.setTipo(dto.getTipo());
        maquina.setMarca(dto.getMarca());
        maquina.setEstado(dto.getEstado());
        
        return convertToDto(maquinaRepository.save(maquina));
    }

    // Convertidor Helper
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