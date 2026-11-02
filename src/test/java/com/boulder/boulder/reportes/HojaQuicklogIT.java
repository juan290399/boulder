package com.boulder.boulder.reportes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.util.UUID;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** Hoja de registro de quicklog contra la base real. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(roles = "VISITANTE")
class HojaQuicklogIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private HojaQuicklogService servicio;

    @Autowired
    private JdbcTemplate jdbc;

    private UUID sondajeConQuicklog() {
        return jdbc.queryForObject("""
                SELECT sondaje_id FROM logueo.iquicklog
                 WHERE eliminado_en IS NULL GROUP BY sondaje_id ORDER BY count(*) DESC LIMIT 1
                """, UUID.class);
    }

    @Test
    @DisplayName("Arma la hoja de un sondaje con quicklog: cabecera y tramos en orden")
    void hoja() {
        HojaQuicklog h = servicio.generar(sondajeConQuicklog());

        assertThat(h.cabecera().sondaje()).startsWith("DDH-");
        assertThat(h.tramos()).isNotEmpty();
        assertThat(h.tramos()).isSortedAccordingTo((a, b) -> a.desde().compareTo(b.desde()));
    }

    @Test
    @DisplayName("Los códigos de los diccionarios salen sin el relleno de char(n)")
    void codigosSinRelleno() {
        HojaQuicklog h = servicio.generar(sondajeConQuicklog());

        assertThat(h.tramos()).allSatisfy(t -> {
            assertThat(t.lito()).doesNotEndWith(" ");
            if (t.ensambleAlteracion() != null) {
                assertThat(t.ensambleAlteracion()).doesNotContain(" ");
            }
        });
    }

    @Test
    @DisplayName("Cualquier sesión descarga el Excel")
    void excel() throws Exception {
        byte[] libro = mvc.perform(get("/api/reportes/quicklog/excel").param("sondajeId", sondajeConQuicklog().toString()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("quicklog_DDH-")))
                .andReturn().getResponse().getContentAsByteArray();
        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(libro))) {
            assertThat(wb.getSheet("Hoja de registro")).isNotNull();
        }
    }

    @Test
    @DisplayName("Un sondaje que no existe responde 404")
    void inexistente() throws Exception {
        mvc.perform(get("/api/reportes/quicklog/excel").param("sondajeId", UUID.randomUUID().toString()))
                .andExpect(status().isNotFound());
    }
}
