package com.boulder.boulder.config;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/** La aplicación Angular servida por el mismo jar que el API (despliegue en un solo artefacto). */
@SpringBootTest
@AutoConfigureMockMvc
class AplicacionWebIT {

    @Autowired
    private MockMvc mvc;

    @ParameterizedTest(name = "GET {0}")
    @ValueSource(strings = {"/reportes", "/reportes/periodo", "/auth/login"})
    @DisplayName("Las rutas de Angular devuelven index.html sin sesión")
    void rutasDeAngular(String ruta) throws Exception {
        mvc.perform(get(ruta))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<app-root>")));
    }

    @Test
    @DisplayName("La raíz entrega la página de bienvenida index.html")
    void raiz() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk()).andExpect(forwardedUrl("index.html"));
    }

    @Test
    @DisplayName("Los archivos estáticos se sirven tal cual")
    void estaticos() throws Exception {
        mvc.perform(get("/main-PRUEBA.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("boulder")));
    }

    @Test
    @DisplayName("Un HEAD a un archivo estático responde como el GET (proxies y monitores)")
    void cabeceraHead() throws Exception {
        mvc.perform(head("/main-PRUEBA.js")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("El API sigue protegido: una ruta /api sin sesión no devuelve index.html")
    void apiProtegida() throws Exception {
        mvc.perform(get("/api/operacional/sondajes")).andExpect(status().isForbidden());
        mvc.perform(get("/api/no-existe")).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Un archivo que no existe responde 404, no index.html")
    void archivoInexistente() throws Exception {
        mvc.perform(get("/no-existe.js")).andExpect(status().isNotFound());
    }
}
