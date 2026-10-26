package com.boulder.boulder.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.boulder.boulder.dominio.Revision;
import com.boulder.boulder.dto.AvanceDensidadDTO;
import com.boulder.boulder.entities.AvanceDensidad;
import com.boulder.boulder.repositories.AvanceDensidadRepository;
import com.boulder.boulder.repositories.AvanceOperativoRepository;

/**
 * Implementacion de los servicios de estufa y calculo de densidad (CU16).
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Service
public class AvanceDensidadServiceImpl
        extends AvanceOperativoServiceBase<AvanceDensidad, AvanceDensidadDTO>
        implements AvanceDensidadService {

    @Autowired
    private AvanceDensidadRepository repository;

    @Override
    protected AvanceOperativoRepository<AvanceDensidad> repositorio() {
        return repository;
    }

    @Override
    protected AvanceDensidad nuevaEntidad() {
        return new AvanceDensidad();
    }

    @Override
    protected AvanceDensidadDTO nuevoDto() {
        return new AvanceDensidadDTO();
    }

    @Override
    protected String nombreParte() {
        return "estufa de densidad";
    }

    @Override
    protected void aplicarDetalle(AvanceDensidadDTO dto, AvanceDensidad entidad) {
        entidad.setResponsable(dto.getResponsable());
        entidad.setEtapa(dto.getEtapa());
        entidad.setNroMuestras(dto.getNroMuestras());
        entidad.setTemperaturaC(dto.getTemperaturaC());
        entidad.setTipoLinea(dto.getTipoLinea());
        entidad.setDesde(dto.getDesde());
        entidad.setHasta(dto.getHasta());
    }

    @Override
    protected void revisar(AvanceDensidadDTO dto, Revision revision) {
        revision.opcion("etapa", dto.getEtapa(), "corte", "secado_1", "secado_2", "pesado", "calculo")
                .tramo("tramo", dto.getDesde(), dto.getHasta());
    }

    @Override
    protected void volcarDetalle(AvanceDensidad entidad, AvanceDensidadDTO dto) {
        dto.setResponsable(entidad.getResponsable());
        dto.setEtapa(entidad.getEtapa());
        dto.setNroMuestras(entidad.getNroMuestras());
        dto.setTemperaturaC(entidad.getTemperaturaC());
        dto.setTipoLinea(entidad.getTipoLinea());
        dto.setDesde(entidad.getDesde());
        dto.setHasta(entidad.getHasta());
    }
}
