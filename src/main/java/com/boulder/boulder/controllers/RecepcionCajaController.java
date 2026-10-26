package com.boulder.boulder.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.boulder.boulder.dto.RecepcionCajaDTO;
import com.boulder.boulder.services.AvanceOperativoService;
import com.boulder.boulder.services.RecepcionCajaService;

/**
 * Controlador REST para recepcion de cajas (UC16).
 *
 * <p>Los verbos los aporta {@link AvanceOperativoControllerBase}.</p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/operacional/recepcion-cajas")
@CrossOrigin(origins = "*")
public class RecepcionCajaController extends AvanceOperativoControllerBase<RecepcionCajaDTO> {

    @Autowired
    private RecepcionCajaService servicio;

    @Override
    protected AvanceOperativoService<RecepcionCajaDTO> servicio() {
        return servicio;
    }

    @Override
    protected String nombreParte() {
        return "recepcion de cajas";
    }
}
