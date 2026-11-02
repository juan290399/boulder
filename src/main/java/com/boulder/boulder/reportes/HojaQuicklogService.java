package com.boulder.boulder.reportes;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

/** Arma la hoja de registro de quicklog de un sondaje. */
@Service
public class HojaQuicklogService {

    public HojaQuicklog generar(UUID sondajeId) {
        return HojaQuicklog.de(null, List.of());
    }
}
