package com.boulder.boulder.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.boulder.boulder.dominio.Revision;
import com.boulder.boulder.dto.AvanceMuestreoDTO;
import com.boulder.boulder.entities.AvanceMuestreo;
import com.boulder.boulder.repositories.AvanceMuestreoRepository;
import com.boulder.boulder.repositories.AvanceOperativoRepository;

/**
 * Implementacion de los servicios de muestreo geoquimico (CU17).
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Service
public class AvanceMuestreoServiceImpl
        extends AvanceOperativoServiceBase<AvanceMuestreo, AvanceMuestreoDTO>
        implements AvanceMuestreoService {

    @Autowired
    private AvanceMuestreoRepository repository;

    @Override
    protected AvanceOperativoRepository<AvanceMuestreo> repositorio() {
        return repository;
    }

    @Override
    protected AvanceMuestreo nuevaEntidad() {
        return new AvanceMuestreo();
    }

    @Override
    protected AvanceMuestreoDTO nuevoDto() {
        return new AvanceMuestreoDTO();
    }

    @Override
    protected String nombreParte() {
        return "muestreo geoquimico";
    }

    @Override
    protected void aplicarDetalle(AvanceMuestreoDTO dto, AvanceMuestreo entidad) {
        entidad.setResponsable(dto.getResponsable());
        entidad.setEstadoMuestreo(dto.getEstadoMuestreo());
        entidad.setDesde(dto.getDesde());
        entidad.setHasta(dto.getHasta());
        entidad.setAvance(dto.getAvance());
        entidad.setNroMuestras(dto.getNroMuestras());
        entidad.setNroMuestrasEmbolsadas(dto.getNroMuestrasEmbolsadas());
        entidad.setNroCostales(dto.getNroCostales());
        entidad.setNroLotes(dto.getNroLotes());
    }

    @Override
    protected void revisar(AvanceMuestreoDTO dto, Revision revision) {
        revision.opcion("estado_muestreo", dto.getEstadoMuestreo(), "en_proceso", "finalizado")
                .tramo("tramo", dto.getDesde(), dto.getHasta())
                .avance(dto.getDesde(), dto.getHasta(), dto.getAvance());
    }

    @Override
    protected void volcarDetalle(AvanceMuestreo entidad, AvanceMuestreoDTO dto) {
        dto.setResponsable(entidad.getResponsable());
        dto.setEstadoMuestreo(entidad.getEstadoMuestreo());
        dto.setDesde(entidad.getDesde());
        dto.setHasta(entidad.getHasta());
        dto.setAvance(entidad.getAvance());
        dto.setNroMuestras(entidad.getNroMuestras());
        dto.setNroMuestrasEmbolsadas(entidad.getNroMuestrasEmbolsadas());
        dto.setNroCostales(entidad.getNroCostales());
        dto.setNroLotes(entidad.getNroLotes());
    }
}
