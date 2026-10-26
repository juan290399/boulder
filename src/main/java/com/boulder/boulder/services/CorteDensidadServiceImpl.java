package com.boulder.boulder.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.boulder.boulder.dto.CorteDensidadDTO;
import com.boulder.boulder.entities.CorteDensidad;
import com.boulder.boulder.repositories.AvanceOperativoRepository;
import com.boulder.boulder.repositories.CorteDensidadRepository;

/**
 * Implementacion de los servicios de corte de densidad (UC20).
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Service
public class CorteDensidadServiceImpl
        extends AvanceOperativoServiceBase<CorteDensidad, CorteDensidadDTO>
        implements CorteDensidadService {

    @Autowired
    private CorteDensidadRepository repository;

    @Override
    protected AvanceOperativoRepository<CorteDensidad> repositorio() {
        return repository;
    }

    @Override
    protected CorteDensidad nuevaEntidad() {
        return new CorteDensidad();
    }

    @Override
    protected CorteDensidadDTO nuevoDto() {
        return new CorteDensidadDTO();
    }

    @Override
    protected String nombreParte() {
        return "corte de densidad";
    }

    @Override
    protected void aplicarDetalle(CorteDensidadDTO dto, CorteDensidad entidad) {
        entidad.setResponsable(dto.getResponsable());
        entidad.setDesde(dto.getDesde());
        entidad.setHasta(dto.getHasta());
        entidad.setTotalMuestras(dto.getTotalMuestras());
        entidad.setTotalMetraje(dto.getTotalMetraje());
        entidad.setTipoLinea(dto.getTipoLinea());
    }

    @Override
    protected void volcarDetalle(CorteDensidad entidad, CorteDensidadDTO dto) {
        dto.setResponsable(entidad.getResponsable());
        dto.setDesde(entidad.getDesde());
        dto.setHasta(entidad.getHasta());
        dto.setTotalMuestras(entidad.getTotalMuestras());
        dto.setTotalMetraje(entidad.getTotalMetraje());
        dto.setTipoLinea(entidad.getTipoLinea());
    }
}
