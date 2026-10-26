package com.boulder.boulder.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.boulder.boulder.dto.AvanceFotografiaDTO;
import com.boulder.boulder.services.AvanceOperativoService;
import com.boulder.boulder.services.AvanceFotografiaService;

/**
 * Controlador REST para registro fotografico (UC17).
 *
 * <p>Los verbos los aporta {@link AvanceOperativoControllerBase}.</p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/operacional/avances-fotografia")
public class AvanceFotografiaController extends AvanceOperativoControllerBase<AvanceFotografiaDTO> {

    @Autowired
    private AvanceFotografiaService servicio;

    @Override
    protected AvanceOperativoService<AvanceFotografiaDTO> servicio() {
        return servicio;
    }

    @Override
    protected String nombreParte() {
        return "registro fotografico";
    }
}
