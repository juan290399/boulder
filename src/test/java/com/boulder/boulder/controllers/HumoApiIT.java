package com.boulder.boulder.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Prueba de humo del API: cada listado responde 200 con JSON contra la base real.
 *
 * <p>No revisa el contenido; revisa que ninguna capa se haya roto (mapeo de una
 * entidad que ya no coincide con su tabla, una consulta con una columna
 * renombrada, un servicio sin inyectar). Es la red que se tiende antes de tocar
 * algo grande.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(roles = "ADMIN")
class HumoApiIT {

    @Autowired
    private MockMvc mvc;

    @ParameterizedTest(name = "GET {0}")
    @ValueSource(strings = {
            "/api/base/usuarios", "/api/base/roles", "/api/base/empleados",
            "/api/operacional/empresas", "/api/operacional/proyectos",
            "/api/operacional/plataformas", "/api/operacional/maquinas",
            "/api/operacional/programas-sondaje", "/api/operacional/collares",
            "/api/operacional/sondajes", "/api/operacional/surveys", "/api/operacional/survey-mediciones",
            "/api/operacional/avances-perforacion", "/api/operacional/recepcion-cajas",
            "/api/operacional/avances-fotografia", "/api/operacional/cortes-muestra",
            "/api/operacional/cortes-densidad", "/api/operacional/avances-densidad",
            "/api/operacional/avances-muestreo", "/api/operacional/avances-logueo",
            "/api/operacional/frente-avance",
            "/api/geologia/catalogos", "/api/geologia/catalogos/estructura",
            "/api/importacion/plantillas", "/api/logueo/dispositivos",
            "/api/reportes/diario?fecha=2026-09-07",
    })
    @DisplayName("Los listados del API responden 200 con JSON")
    void listados(String ruta) throws Exception {
        mvc.perform(get(ruta))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }
}
