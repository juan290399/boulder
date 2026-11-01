package com.boulder.boulder.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** Permisos de escritura por rol sobre la cadena de filtros real. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PermisosApiIT {

    @Autowired
    private MockMvc mvc;

    @Test
    @WithMockUser(roles = "VISITANTE")
    @DisplayName("El visitante lee pero no sube logueo")
    void visitante() throws Exception {
        mvc.perform(get("/api/operacional/sondajes")).andExpect(status().isOk());
        mvc.perform(put("/api/logueo/quicklog").contentType(MediaType.APPLICATION_JSON).content("[]"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ASISTENTE_GEOLOGIA")
    @DisplayName("El asistente sube logueo pero no borra partes")
    void asistente() throws Exception {
        mvc.perform(put("/api/logueo/quicklog").contentType(MediaType.APPLICATION_JSON).content("[]"))
                .andExpect(status().isNoContent());
        mvc.perform(delete("/api/operacional/avances-perforacion/" + UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "GEOLOGO")
    @DisplayName("El geólogo pasa la autorización para borrar un parte")
    void geologo() throws Exception {
        int estado = mvc.perform(delete("/api/operacional/avances-perforacion/" + UUID.randomUUID()))
                .andReturn().getResponse().getStatus();
        assertThat(estado).isNotEqualTo(403);
    }
}
