package com.boulder.boulder.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/** Endpoints de monitoreo (Actuator) sobre la cadena de seguridad real. */
@SpringBootTest
@AutoConfigureMockMvc
class MonitoreoIT {

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("La salud responde sin sesión, solo con el estado")
    void saludPublica() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN ve el detalle de la salud: base de datos y disco")
    void saludDetallada() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.db.status").value("UP"))
                .andExpect(jsonPath("$.components.diskSpace.status").value("UP"));
    }

    @Test
    @DisplayName("Las métricas exigen sesión")
    void metricasSinSesion() throws Exception {
        mvc.perform(get("/actuator/metrics")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "GEOLOGO")
    @DisplayName("Las métricas son solo para ADMIN")
    void metricasSinAdmin() throws Exception {
        mvc.perform(get("/actuator/metrics")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN consulta métricas de la JVM, del pool de conexiones y de las peticiones HTTP")
    void metricasAdmin() throws Exception {
        mvc.perform(get("/actuator/metrics/jvm.memory.used")).andExpect(status().isOk());
        mvc.perform(get("/actuator/metrics/hikaricp.connections.active")).andExpect(status().isOk());
        mvc.perform(get("/actuator/metrics/process.cpu.usage")).andExpect(status().isOk());
    }
}
