package com.boulder.boulder.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.boulder.boulder.dto.AvancePerforacionDTO;
import com.boulder.boulder.services.AvancePerforacionService;

/** Capa web del parte de perforación; el servicio es un mock. */
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
class AvancePerforacionControllerTest {

    private static final String URL = "/api/operacional/avances-perforacion";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private AvancePerforacionService servicio;

    @Test
    @DisplayName("GET /sugerencia devuelve el borrador del turno siguiente")
    void sugerencia() throws Exception {
        UUID sondajeId = UUID.randomUUID();
        AvancePerforacionDTO borrador = new AvancePerforacionDTO();
        borrador.setSondajeId(sondajeId);
        borrador.setDesde(new BigDecimal("135.20"));
        when(servicio.sugerencia(sondajeId)).thenReturn(borrador);

        mvc.perform(get(URL + "/sugerencia").param("sondajeId", sondajeId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.desde").value(135.20));
    }

    @Test
    @DisplayName("Un parte sin fecha ni profundidades no llega al servicio")
    void cuerpoIncompleto() throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sondajeId\":\"" + UUID.randomUUID() + "\"}"))
                .andExpect(status().isBadRequest());
        verify(servicio, never()).guardar(any());
    }

    @Test
    @DisplayName("Si el turno ya está cargado, la respuesta es 400 con el motivo")
    void duplicado() throws Exception {
        when(servicio.guardar(any())).thenThrow(new IllegalStateException("Ese turno ya está cargado"));

        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sondajeId\":\"" + UUID.randomUUID()
                                + "\",\"fecha\":\"2026-10-26\",\"turno\":\"dia\",\"desde\":0,\"hasta\":10,\"avance\":10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ese turno ya está cargado"));
    }
}
