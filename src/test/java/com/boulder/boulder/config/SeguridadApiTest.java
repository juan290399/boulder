package com.boulder.boulder.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Pruebas de integración de la capa de seguridad, con el contexto completo y la
 * cadena de filtros real.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SeguridadApiTest {

    @Autowired
    private MockMvc mvc;

    @ParameterizedTest(name = "{0} sin token se rechaza")
    @ValueSource(strings = {"/api/operacional/sondajes", "/api/logueo/quicklog", "/api/fotos", "/api/base/usuarios"})
    @DisplayName("El API de datos exige token")
    void sinTokenSeRechaza(String ruta) throws Exception {
        mvc.perform(get(ruta)).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Un token inventado no abre el API")
    void tokenFalso() throws Exception {
        mvc.perform(get("/api/operacional/sondajes").header("Authorization", "Bearer abc.def.ghi"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("El sexto intento fallido seguido recibe 429 con un mensaje claro")
    void bloqueoPorIntentos() throws Exception {
        String cuerpo = "{\"usuario\":\"prueba_bloqueo_tdd\",\"contrasenia\":\"incorrecta\"}";
        for (int i = 0; i < 5; i++) {
            mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                    .andExpect(status().isUnauthorized());
        }
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    @DisplayName("CORS: el frontend configurado pasa; un origen ajeno se rechaza")
    void cors() throws Exception {
        mvc.perform(options("/api/operacional/sondajes")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"));

        mvc.perform(options("/api/operacional/sondajes")
                        .header("Origin", "https://sitio-ajeno.example")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("El usuario que llega a la bitácora no puede inventar líneas")
    void bitacoraSinSaltos() {
        assertThat(JwtAuthenticationFilter.seguro("admin\nlogin correcto usuario=root"))
                .doesNotContain("\n")
                .startsWith("admin_");
        assertThat(JwtAuthenticationFilter.seguro("x".repeat(500))).hasSizeLessThanOrEqualTo(64);
    }
}
