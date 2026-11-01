package com.boulder.boulder.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PermisosEscrituraTest {

    private static Set<String> roles(String... r) {
        return Set.of(r);
    }

    @ParameterizedTest(name = "{0} {1}")
    @CsvSource({
            "GET,  /api/operacional/sondajes",
            "GET,  /api/logueo/quicklog",
            "HEAD, /api/fotos/abc",
    })
    @DisplayName("Cualquier rol con sesión lee")
    void lectura(String metodo, String ruta) {
        assertThat(PermisosEscritura.permite(metodo, ruta, roles("ROLE_VISITANTE"))).isTrue();
    }

    @ParameterizedTest(name = "{0} {1}")
    @CsvSource({
            "POST,   /api/operacional/avances-perforacion",
            "PUT,    /api/logueo/quicklog",
            "POST,   /api/importacion/quicklog",
            "POST,   /api/fotos",
            "DELETE, /api/operacional/avances-logueo/1",
    })
    @DisplayName("El visitante no escribe")
    void visitante(String metodo, String ruta) {
        assertThat(PermisosEscritura.permite(metodo, ruta, roles("ROLE_VISITANTE"))).isFalse();
    }

    @ParameterizedTest(name = "{0} {1}")
    @CsvSource({
            "POST,  /api/operacional/avances-perforacion",
            "PUT,   /api/operacional/survey-mediciones/7",
            "PUT,   /api/logueo/litologia",
            "POST,  /api/importacion/perforacion",
            "POST,  /api/fotos",
    })
    @DisplayName("El asistente de geología registra y corrige partes, logueo, cargas y fotos")
    void asistenteRegistra(String metodo, String ruta) {
        assertThat(PermisosEscritura.permite(metodo, ruta, roles("ROLE_ASISTENTE_GEOLOGIA"))).isTrue();
    }

    @ParameterizedTest(name = "{0} {1}")
    @CsvSource({
            "DELETE, /api/operacional/avances-perforacion/3",
            "POST,   /api/operacional/sondajes",
            "PUT,    /api/operacional/maquinas/2",
            "PUT,    /api/operacional/plataformas/2",
            "POST,   /api/operacional/programas-sondaje",
            "PUT,    /api/operacional/collares/9",
    })
    @DisplayName("El asistente no borra partes ni toca datos maestros")
    void asistenteNoBorraNiMaestros(String metodo, String ruta) {
        assertThat(PermisosEscritura.permite(metodo, ruta, roles("ROLE_ASISTENTE_GEOLOGIA"))).isFalse();
    }

    @ParameterizedTest(name = "{0} {1}")
    @CsvSource({
            "DELETE, /api/operacional/avances-perforacion/3",
            "POST,   /api/operacional/sondajes",
            "PUT,    /api/operacional/collares/9",
            "PUT,    /api/logueo/estructural",
    })
    @DisplayName("El geólogo borra partes y mantiene los datos maestros")
    void geologo(String metodo, String ruta) {
        assertThat(PermisosEscritura.permite(metodo, ruta, roles("ROLE_GEOLOGO"))).isTrue();
    }

    @Test
    @DisplayName("ADMIN escribe en todo")
    void admin() {
        assertThat(PermisosEscritura.permite("DELETE", "/api/operacional/sondajes/1", roles("ROLE_ADMIN"))).isTrue();
    }

    @Test
    @DisplayName("Con varios roles basta que uno permita")
    void variosRoles() {
        assertThat(PermisosEscritura.permite("POST", "/api/operacional/sondajes",
                roles("ROLE_VISITANTE", "ROLE_GEOLOGO"))).isTrue();
    }

    @Test
    @DisplayName("Sin roles no se escribe")
    void sinRoles() {
        assertThat(PermisosEscritura.permite("PUT", "/api/logueo/quicklog", Set.of())).isFalse();
    }
}
