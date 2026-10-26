package com.boulder.boulder.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.boulder.boulder.dominio.Revision;
import com.boulder.boulder.dto.AvanceLogueoDTO;
import com.boulder.boulder.entities.AvanceLogueo;
import com.boulder.boulder.repositories.AvanceLogueoRepository;
import com.boulder.boulder.repositories.AvanceOperativoRepository;

/**
 * Implementacion de los servicios de partes de avance de logueo (UC16).
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Service
public class AvanceLogueoServiceImpl
        extends AvanceOperativoServiceBase<AvanceLogueo, AvanceLogueoDTO>
        implements AvanceLogueoService {

    @Autowired
    private AvanceLogueoRepository repository;

    @Override
    protected AvanceOperativoRepository<AvanceLogueo> repositorio() {
        return repository;
    }

    @Override
    protected AvanceLogueo nuevaEntidad() {
        return new AvanceLogueo();
    }

    @Override
    protected AvanceLogueoDTO nuevoDto() {
        return new AvanceLogueoDTO();
    }

    @Override
    protected String nombreParte() {
        return "avance de logueo";
    }

    @Override
    protected void aplicarDetalle(AvanceLogueoDTO dto, AvanceLogueo entidad) {
        entidad.setTipoLogueo(dto.getTipoLogueo());
        entidad.setDesde(dto.getDesde());
        entidad.setHasta(dto.getHasta());
        entidad.setAvance(dto.getAvance());
        entidad.setGeologo(dto.getGeologo());
        entidad.setPctCompletado(dto.getPctCompletado());
    }

    @Override
    protected void revisar(AvanceLogueoDTO dto, Revision revision) {
        revision.opcion("tipo_logueo", dto.getTipoLogueo(), "geologico", "geotecnico", "estructural", "rapido")
                .tramo("tramo", dto.getDesde(), dto.getHasta())
                .avance(dto.getDesde(), dto.getHasta(), dto.getAvance())
                .porcentaje("pct_completado", dto.getPctCompletado());
    }

    @Override
    protected void volcarDetalle(AvanceLogueo entidad, AvanceLogueoDTO dto) {
        dto.setTipoLogueo(entidad.getTipoLogueo());
        dto.setDesde(entidad.getDesde());
        dto.setHasta(entidad.getHasta());
        dto.setAvance(entidad.getAvance());
        dto.setGeologo(entidad.getGeologo());
        dto.setPctCompletado(entidad.getPctCompletado());
    }
}
