package com.boulder.boulder.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.boulder.boulder.dto.CorteMuestraDTO;
import com.boulder.boulder.services.AvanceOperativoService;
import com.boulder.boulder.services.CorteMuestraService;

/**
 * Controlador REST para corte geoquimico (UC18).
 *
 * <p>Los verbos los aporta {@link AvanceOperativoControllerBase}.</p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/operacional/cortes-muestra")
@CrossOrigin(origins = "*")
public class CorteMuestraController extends AvanceOperativoControllerBase<CorteMuestraDTO> {

    @Autowired
    private CorteMuestraService servicio;

    @Override
    protected AvanceOperativoService<CorteMuestraDTO> servicio() {
        return servicio;
    }

    @Override
    protected String nombreParte() {
        return "corte geoquimico";
    }
}
