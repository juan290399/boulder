package com.boulder.boulder.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.boulder.boulder.dominio.Revision;
import com.boulder.boulder.dto.CorteMuestraDTO;
import com.boulder.boulder.entities.CorteMuestra;
import com.boulder.boulder.repositories.AvanceOperativoRepository;
import com.boulder.boulder.repositories.CorteMuestraRepository;

/**
 * Implementacion de los servicios de corte geoquimico (CU14).
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Service
public class CorteMuestraServiceImpl
        extends AvanceOperativoServiceBase<CorteMuestra, CorteMuestraDTO>
        implements CorteMuestraService {

    @Autowired
    private CorteMuestraRepository repository;

    @Override
    protected AvanceOperativoRepository<CorteMuestra> repositorio() {
        return repository;
    }

    @Override
    protected CorteMuestra nuevaEntidad() {
        return new CorteMuestra();
    }

    @Override
    protected CorteMuestraDTO nuevoDto() {
        return new CorteMuestraDTO();
    }

    @Override
    protected String nombreParte() {
        return "corte geoquimico";
    }

    @Override
    protected void aplicarDetalle(CorteMuestraDTO dto, CorteMuestra entidad) {
        entidad.setResponsable(dto.getResponsable());
        entidad.setDesde(dto.getDesde());
        entidad.setHasta(dto.getHasta());
        entidad.setTipoLinea(dto.getTipoLinea());
        entidad.setMetrajeEstimado(dto.getMetrajeEstimado());
        entidad.setNroMuestras(dto.getNroMuestras());
    }

    @Override
    protected void revisar(CorteMuestraDTO dto, Revision revision) {
        revision.tramo("tramo", dto.getDesde(), dto.getHasta());
    }

    @Override
    protected void volcarDetalle(CorteMuestra entidad, CorteMuestraDTO dto) {
        dto.setResponsable(entidad.getResponsable());
        dto.setDesde(entidad.getDesde());
        dto.setHasta(entidad.getHasta());
        dto.setTipoLinea(entidad.getTipoLinea());
        dto.setMetrajeEstimado(entidad.getMetrajeEstimado());
        dto.setNroMuestras(entidad.getNroMuestras());
    }
}
