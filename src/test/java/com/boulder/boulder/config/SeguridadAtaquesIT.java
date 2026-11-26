package com.boulder.boulder.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Pruebas de seguridad del API: tokens manipulados, inyección SQL, cabeceras,
 * fuga de información en los errores y asignación masiva en el auto-registro.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SeguridadAtaquesIT {

    private static final String RUTA = "/api/operacional/sondajes";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Value("${jwt.secret}")
    private String secreto;

    private static String b64(String s) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    private String usuarioActivo() {
        return jdbc.queryForObject("SELECT usuario FROM base.usuario WHERE estado = true LIMIT 1", String.class);
    }

    @Test
    @DisplayName("Un token sin firma (alg none) no abre el API")
    void algNone() throws Exception {
        String token = b64("{\"alg\":\"none\"}") + "." + b64("{\"sub\":\"" + usuarioActivo()
                + "\",\"roles\":[\"ROLE_ADMIN\"]}") + ".";
        mvc.perform(get(RUTA).header("Authorization", "Bearer " + token)).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Un token firmado con otra clave no abre el API")
    void otraClave() throws Exception {
        SecretKey ajena = Keys.hmacShaKeyFor("una-clave-que-no-es-la-del-servidor-0123456789".getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder().subject(usuarioActivo()).claim("roles", List.of("ROLE_ADMIN"))
                .expiration(new Date(System.currentTimeMillis() + 60_000)).signWith(ajena).compact();
        mvc.perform(get(RUTA).header("Authorization", "Bearer " + token)).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Un token vencido no abre el API aunque la firma sea válida")
    void vencido() throws Exception {
        SecretKey clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder().subject(usuarioActivo()).claim("roles", List.of("ROLE_ADMIN"))
                .issuedAt(new Date(System.currentTimeMillis() - 7_200_000))
                .expiration(new Date(System.currentTimeMillis() - 3_600_000)).signWith(clave).compact();
        mvc.perform(get(RUTA).header("Authorization", "Bearer " + token)).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Un token válido con los roles alterados a mano pierde la firma")
    void rolesAlterados() throws Exception {
        SecretKey clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        String valido = Jwts.builder().subject(usuarioActivo()).claim("roles", List.of("ROLE_VISITANTE"))
                .expiration(new Date(System.currentTimeMillis() + 60_000)).signWith(clave).compact();
        String[] partes = valido.split("\\.");
        String alterado = partes[0] + "." + b64("{\"sub\":\"" + usuarioActivo() + "\",\"roles\":[\"ROLE_ADMIN\"]}")
                + "." + partes[2];
        mvc.perform(get("/api/base/usuarios").header("Authorization", "Bearer " + alterado))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest(name = "codigo = {0}")
    @ValueSource(strings = {"' OR '1'='1", "x'; DROP TABLE operacional.prf_sondaje; --", "\" OR 1=1 --"})
    @WithMockUser(roles = "VISITANTE")
    @DisplayName("Un parámetro con SQL no se ejecuta: responde vacío y la tabla sigue ahí")
    void inyeccionSql(String ataque) throws Exception {
        mvc.perform(get("/api/operacional/desviaciones/sondaje").param("codigo", ataque))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM operacional.prf_sondaje", Long.class)).isPositive();
    }

    @Test
    @DisplayName("Las respuestas llevan cabeceras contra sniffing, clickjacking y caché de datos")
    void cabeceras() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().exists("Cache-Control"));
    }

    @Test
    @WithMockUser(roles = "ASISTENTE_GEOLOGIA")
    @DisplayName("Un JSON roto responde 400 sin la traza ni el nombre de las clases")
    void errorSinFuga() throws Exception {
        String cuerpo = mvc.perform(post("/api/operacional/avances-perforacion")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"fecha\": "))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();
        assertThat(cuerpo).doesNotContain("Exception").doesNotContain("com.boulder").doesNotContain("\tat ");
    }

    @Test
    @DisplayName("El auto-registro ignora roles y estado enviados en el cuerpo: la cuenta nace inactiva y sin roles")
    void asignacionMasiva() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
                        {"nombres":"Prueba","apellidos":"Seguridad","correo":"ataque_tdd@boulder.pe",
                         "usuario":"ataque_tdd","contrasenia":"Clave2026x",
                         "estado":true,"roles":["ADMIN"]}
                        """))
                .andExpect(status().is2xxSuccessful());
        assertThat(jdbc.queryForObject("SELECT estado FROM base.usuario WHERE usuario = 'ataque_tdd'", Boolean.class))
                .isFalse();
        assertThat(jdbc.queryForObject("""
                SELECT count(*) FROM base.usuario_rol ur JOIN base.usuario u ON u.id = ur.id_usuario
                 WHERE u.usuario = 'ataque_tdd'
                """, Long.class)).isZero();
    }
}
