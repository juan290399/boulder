package com.boulder.boulder.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.boulder.boulder.dto.AvanceLogueoDTO;
import com.boulder.boulder.services.AvanceOperativoService;
import com.boulder.boulder.services.AvanceLogueoService;

/**
 * Controlador REST para avance de logueo (CU18).
 *
 * <p>Los verbos los aporta {@link AvanceOperativoControllerBase}.</p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/operacional/avances-logueo")
public class AvanceLogueoController extends AvanceOperativoControllerBase<AvanceLogueoDTO> {

    @Autowired
    private AvanceLogueoService servicio;

    @Override
    protected AvanceOperativoService<AvanceLogueoDTO> servicio() {
        return servicio;
    }

    @Override
    protected String nombreParte() {
        return "avance de logueo";
    }
}
