package com.boulder.boulder.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.boulder.boulder.services.CambioContrasenaService;

/**
 * El controlador solo traduce HTTP: las reglas viven en el servicio, que aquí es
 * un mock. Lo que se prueba es la capa web (MVC) y su seguridad.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CuentaControllerTest {

    private static final String CUERPO = "{\"actual\":\"Actual2026\",\"nueva\":\"Testigo2027\"}";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CambioContrasenaService servicio;

    @Test
    @DisplayName("Sin sesión no se puede cambiar ninguna contraseña")
    void sinSesion() throws Exception {
        mvc.perform(put("/api/cuenta/contrasena").contentType(MediaType.APPLICATION_JSON).content(CUERPO))
                .andExpect(status().isForbidden());
        verify(servicio, never()).cambiar(any(), any(), any());
    }

    @Test
    @WithMockUser(username = "jvelazco", roles = "VISITANTE")
    @DisplayName("Cualquier rol cambia la suya; el usuario sale de la sesión, no del cuerpo")
    void cambiaLaPropia() throws Exception {
        mvc.perform(put("/api/cuenta/contrasena").contentType(MediaType.APPLICATION_JSON).content(CUERPO))
                .andExpect(status().isNoContent());
        verify(servicio).cambiar("jvelazco", "Actual2026", "Testigo2027");
    }

    @Test
    @WithMockUser(username = "jvelazco")
    @DisplayName("Si el servicio rechaza el cambio, la respuesta es 400 con el motivo")
    void rechazo() throws Exception {
        doThrow(new IllegalArgumentException("La contraseña actual no es correcta."))
                .when(servicio).cambiar(any(), any(), any());

        mvc.perform(put("/api/cuenta/contrasena").contentType(MediaType.APPLICATION_JSON).content(CUERPO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("La contraseña actual no es correcta."));
    }

    @Test
    @WithMockUser(username = "jvelazco")
    @DisplayName("Un cuerpo sin la contraseña nueva no llega al servicio")
    void cuerpoIncompleto() throws Exception {
        mvc.perform(put("/api/cuenta/contrasena").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actual\":\"Actual2026\"}"))
                .andExpect(status().isBadRequest());
        verify(servicio, never()).cambiar(any(), any(), any());
    }
}
