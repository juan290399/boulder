package com.boulder.boulder.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.boulder.boulder.dto.AvanceDensidadDTO;
import com.boulder.boulder.services.AvanceOperativoService;
import com.boulder.boulder.services.AvanceDensidadService;

/**
 * Controlador REST para estufa de densidad (CU16).
 *
 * <p>Los verbos los aporta {@link AvanceOperativoControllerBase}.</p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/operacional/avances-densidad")
public class AvanceDensidadController extends AvanceOperativoControllerBase<AvanceDensidadDTO> {

    @Autowired
    private AvanceDensidadService servicio;

    @Override
    protected AvanceOperativoService<AvanceDensidadDTO> servicio() {
        return servicio;
    }

    @Override
    protected String nombreParte() {
        return "estufa de densidad";
    }
}
