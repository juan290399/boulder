package com.boulder.boulder.reportes;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;

/** Arma la hoja de registro de quicklog de un sondaje. */
@Service
public class HojaQuicklogService {

    private final HojaQuicklogRepository repositorio;

    public HojaQuicklogService(HojaQuicklogRepository repositorio) {
        this.repositorio = repositorio;
    }

    /**
     * @param sondajeId sondaje
     * @return la hoja con la cabecera y los tramos vigentes
     * @throws EntityNotFoundException si el sondaje no existe
     */
    @Transactional(readOnly = true)
    public HojaQuicklog generar(UUID sondajeId) {
        return HojaQuicklog.de(
                repositorio.cabecera(sondajeId).orElseThrow(() -> new EntityNotFoundException("Sondaje no encontrado: " + sondajeId)),
                repositorio.tramos(sondajeId));
    }
}
