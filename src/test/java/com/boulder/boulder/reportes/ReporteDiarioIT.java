package com.boulder.boulder.reportes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integración de punta a punta: MockMvc, seguridad, servicio, DAO y la base
 * PostgreSQL real con los partes del 7 de septiembre de 2026.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ReporteDiarioIT {

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("Sin sesión no hay reporte")
    void sinSesion() throws Exception {
        mvc.perform(get("/api/reportes/diario").param("fecha", "2026-09-07")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "VISITANTE")
    @DisplayName("La gerencia ve el día con sus totales y la serie de siete días")
    void json() throws Exception {
        mvc.perform(get("/api/reportes/diario").param("fecha", "2026-09-07"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fecha").value("2026-09-07"))
                .andExpect(jsonPath("$.sondajes").isArray())
                .andExpect(jsonPath("$.ultimosDias.length()").value(7))
                .andExpect(jsonPath("$.totalPerforado").isNumber());
    }

    @Test
    @WithMockUser(roles = "VISITANTE")
    @DisplayName("El Excel se descarga como adjunto y se abre con POI")
    void excel() throws Exception {
        byte[] cuerpo = mvc.perform(get("/api/reportes/diario/excel").param("fecha", "2026-09-07"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("reporte_diario_2026-09-07.xlsx")))
                .andReturn().getResponse().getContentAsByteArray();

        try (XSSFWorkbook libro = new XSSFWorkbook(new ByteArrayInputStream(cuerpo))) {
            assertThat(libro.getSheet("2026-09-07")).isNotNull();
        }
    }

    @Test
    @WithMockUser(roles = "VISITANTE")
    @DisplayName("Una fecha futura responde 400")
    void futuro() throws Exception {
        mvc.perform(get("/api/reportes/diario").param("fecha", "2099-01-01")).andExpect(status().isBadRequest());
    }
}
