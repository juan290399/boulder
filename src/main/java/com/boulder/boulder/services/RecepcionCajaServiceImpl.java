package com.boulder.boulder.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.boulder.boulder.dominio.Revision;
import com.boulder.boulder.dto.RecepcionCajaDTO;
import com.boulder.boulder.entities.RecepcionCaja;
import com.boulder.boulder.repositories.AvanceOperativoRepository;
import com.boulder.boulder.repositories.RecepcionCajaRepository;

/**
 * Implementacion de los servicios de recepcion de cajas de campo (UC16).
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Service
public class RecepcionCajaServiceImpl
        extends AvanceOperativoServiceBase<RecepcionCaja, RecepcionCajaDTO>
        implements RecepcionCajaService {

    @Autowired
    private RecepcionCajaRepository repository;

    @Override
    protected AvanceOperativoRepository<RecepcionCaja> repositorio() {
        return repository;
    }

    @Override
    protected RecepcionCaja nuevaEntidad() {
        return new RecepcionCaja();
    }

    @Override
    protected RecepcionCajaDTO nuevoDto() {
        return new RecepcionCajaDTO();
    }

    @Override
    protected String nombreParte() {
        return "recepcion de cajas";
    }

    @Override
    protected void aplicarDetalle(RecepcionCajaDTO dto, RecepcionCaja entidad) {
        entidad.setResponsable(dto.getResponsable());
        entidad.setBoxDesde(dto.getBoxDesde());
        entidad.setBoxHasta(dto.getBoxHasta());
        entidad.setTotalCajas(dto.getTotalCajas());
        entidad.setMarcadoDesde(dto.getMarcadoDesde());
        entidad.setMarcadoHasta(dto.getMarcadoHasta());
        entidad.setTotalMetraje(dto.getTotalMetraje());
    }

    @Override
    protected void revisar(RecepcionCajaDTO dto, Revision revision) {
        revision.cajas("cajas", dto.getBoxDesde(), dto.getBoxHasta())
                .tramo("marcado", dto.getMarcadoDesde(), dto.getMarcadoHasta());
    }

    @Override
    protected void volcarDetalle(RecepcionCaja entidad, RecepcionCajaDTO dto) {
        dto.setResponsable(entidad.getResponsable());
        dto.setBoxDesde(entidad.getBoxDesde());
        dto.setBoxHasta(entidad.getBoxHasta());
        dto.setTotalCajas(entidad.getTotalCajas());
        dto.setMarcadoDesde(entidad.getMarcadoDesde());
        dto.setMarcadoHasta(entidad.getMarcadoHasta());
        dto.setTotalMetraje(entidad.getTotalMetraje());
    }
}
