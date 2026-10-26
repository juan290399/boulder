package com.boulder.boulder.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.boulder.boulder.dto.AvanceMuestreoDTO;
import com.boulder.boulder.services.AvanceOperativoService;
import com.boulder.boulder.services.AvanceMuestreoService;

/**
 * Controlador REST para muestreo geoquimico (UC19).
 *
 * <p>Los verbos los aporta {@link AvanceOperativoControllerBase}.</p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/operacional/avances-muestreo")
@CrossOrigin(origins = "*")
public class AvanceMuestreoController extends AvanceOperativoControllerBase<AvanceMuestreoDTO> {

    @Autowired
    private AvanceMuestreoService servicio;

    @Override
    protected AvanceOperativoService<AvanceMuestreoDTO> servicio() {
        return servicio;
    }

    @Override
    protected String nombreParte() {
        return "muestreo geoquimico";
    }
}
