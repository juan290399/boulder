package com.boulder.boulder.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.boulder.boulder.dto.CorteDensidadDTO;
import com.boulder.boulder.services.AvanceOperativoService;
import com.boulder.boulder.services.CorteDensidadService;

/**
 * Controlador REST para corte de densidad (CU15).
 *
 * <p>Los verbos los aporta {@link AvanceOperativoControllerBase}.</p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/operacional/cortes-densidad")
public class CorteDensidadController extends AvanceOperativoControllerBase<CorteDensidadDTO> {

    @Autowired
    private CorteDensidadService servicio;

    @Override
    protected AvanceOperativoService<CorteDensidadDTO> servicio() {
        return servicio;
    }

    @Override
    protected String nombreParte() {
        return "corte de densidad";
    }
}
