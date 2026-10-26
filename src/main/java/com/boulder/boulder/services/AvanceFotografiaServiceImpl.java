package com.boulder.boulder.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.boulder.boulder.dto.AvanceFotografiaDTO;
import com.boulder.boulder.entities.AvanceFotografia;
import com.boulder.boulder.repositories.AvanceFotografiaRepository;
import com.boulder.boulder.repositories.AvanceOperativoRepository;

/**
 * Implementacion de los servicios de registro fotografico (UC17).
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Service
public class AvanceFotografiaServiceImpl
        extends AvanceOperativoServiceBase<AvanceFotografia, AvanceFotografiaDTO>
        implements AvanceFotografiaService {

    @Autowired
    private AvanceFotografiaRepository repository;

    @Override
    protected AvanceOperativoRepository<AvanceFotografia> repositorio() {
        return repository;
    }

    @Override
    protected AvanceFotografia nuevaEntidad() {
        return new AvanceFotografia();
    }

    @Override
    protected AvanceFotografiaDTO nuevoDto() {
        return new AvanceFotografiaDTO();
    }

    @Override
    protected String nombreParte() {
        return "registro fotografico";
    }

    @Override
    protected void aplicarDetalle(AvanceFotografiaDTO dto, AvanceFotografia entidad) {
        entidad.setResponsable(dto.getResponsable());
        entidad.setBoxDesde(dto.getBoxDesde());
        entidad.setBoxHasta(dto.getBoxHasta());
        entidad.setTotalCajas(dto.getTotalCajas());
        entidad.setMarcadoDesde(dto.getMarcadoDesde());
        entidad.setMarcadoHasta(dto.getMarcadoHasta());
        entidad.setTotalMetraje(dto.getTotalMetraje());
        entidad.setSecoHumedo(dto.getSecoHumedo());
        entidad.setNroFotos(dto.getNroFotos());
        entidad.setRutaArchivos(dto.getRutaArchivos());
    }

    @Override
    protected void volcarDetalle(AvanceFotografia entidad, AvanceFotografiaDTO dto) {
        dto.setResponsable(entidad.getResponsable());
        dto.setBoxDesde(entidad.getBoxDesde());
        dto.setBoxHasta(entidad.getBoxHasta());
        dto.setTotalCajas(entidad.getTotalCajas());
        dto.setMarcadoDesde(entidad.getMarcadoDesde());
        dto.setMarcadoHasta(entidad.getMarcadoHasta());
        dto.setTotalMetraje(entidad.getTotalMetraje());
        dto.setSecoHumedo(entidad.getSecoHumedo());
        dto.setNroFotos(entidad.getNroFotos());
        dto.setRutaArchivos(entidad.getRutaArchivos());
    }
}
