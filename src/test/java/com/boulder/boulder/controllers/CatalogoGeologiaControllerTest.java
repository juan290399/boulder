package com.boulder.boulder.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
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

import com.boulder.boulder.dto.CatalogoGeologiaDTO;
import com.boulder.boulder.services.CatalogoGeologiaService;

/** Permisos del API de diccionarios: todos leen, solo ADMIN escribe. */
@SpringBootTest
@AutoConfigureMockMvc
class CatalogoGeologiaControllerTest {

    private static final String CUERPO = "{\"codigo\":\"MO\",\"nombre\":\"Moderada\"}";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CatalogoGeologiaService servicio;

    @Test
    @WithMockUser(roles = "GEOLOGO")
    @DisplayName("Un geólogo descarga los catálogos")
    void geologoLee() throws Exception {
        when(servicio.listarTodos()).thenReturn(List.of());
        mvc.perform(get("/api/geologia/catalogos")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "GEOLOGO")
    @DisplayName("La estructura de cada catálogo se publica para armar el formulario")
    void estructura() throws Exception {
        when(servicio.estructura()).thenReturn(java.util.Map.of("lit_diccionario",
                new com.boulder.boulder.repositories.CatalogoGeologiaRepository.Estructura(
                        java.util.Set.of("grupo_id", "color_r", "esta_activo"), 4)));
        mvc.perform(get("/api/geologia/catalogos/estructura"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$.lit_diccionario.largoCodigo").value(4));
    }

    @Test
    @WithMockUser(roles = "GEOLOGO")
    @DisplayName("Un geólogo no puede dar de alta códigos")
    void geologoNoEscribe() throws Exception {
        mvc.perform(post("/api/geologia/catalogos/alt_intensidad")
                        .contentType(MediaType.APPLICATION_JSON).content(CUERPO))
                .andExpect(status().isForbidden());
        verify(servicio, never()).crear(any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("El administrador da de alta un código y recibe 201")
    void adminCrea() throws Exception {
        when(servicio.crear(eq("alt_intensidad"), any())).thenReturn(new CatalogoGeologiaDTO());
        mvc.perform(post("/api/geologia/catalogos/alt_intensidad")
                        .contentType(MediaType.APPLICATION_JSON).content(CUERPO))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Desactivar responde 204")
    void adminDesactiva() throws Exception {
        UUID id = UUID.randomUUID();
        mvc.perform(patch("/api/geologia/catalogos/min_zona/" + id + "/estado").param("activo", "false"))
                .andExpect(status().isNoContent());
        verify(servicio).cambiarEstado("min_zona", id, false);
    }
}
