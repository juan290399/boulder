package com.boulder.boulder.monitoreo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.FilterChain;

class FiltroIdPeticionTest {

    private final FiltroIdPeticion filtro = new FiltroIdPeticion();

    private String ejecutar(MockHttpServletRequest req, MockHttpServletResponse res) throws Exception {
        AtomicReference<String> visto = new AtomicReference<>();
        FilterChain cadena = (rq, rs) -> visto.set(MDC.get(FiltroIdPeticion.CLAVE_MDC));
        filtro.doFilter(req, res, cadena);
        return visto.get();
    }

    @Test
    @DisplayName("Sin cabecera: genera un id, lo pone en el MDC durante la petición y lo devuelve")
    void generaId() throws Exception {
        MockHttpServletResponse res = new MockHttpServletResponse();
        String id = ejecutar(new MockHttpServletRequest("GET", "/api/x"), res);

        assertThat(id).isNotBlank().hasSizeLessThanOrEqualTo(64);
        assertThat(res.getHeader(FiltroIdPeticion.CABECERA)).isEqualTo(id);
        assertThat(MDC.get(FiltroIdPeticion.CLAVE_MDC)).isNull();
    }

    @Test
    @DisplayName("Respeta el id que manda el proxy si es válido")
    void respetaIdValido() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/x");
        req.addHeader(FiltroIdPeticion.CABECERA, "nginx-4f2a9c");

        assertThat(ejecutar(req, new MockHttpServletResponse())).isEqualTo("nginx-4f2a9c");
    }

    @Test
    @DisplayName("Un id con saltos de línea u otros caracteres se reemplaza (inyección en la bitácora)")
    void rechazaIdMalicioso() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/x");
        req.addHeader(FiltroIdPeticion.CABECERA, "abc\n2026-11-07 ERROR falso");

        String id = ejecutar(req, new MockHttpServletResponse());

        assertThat(id).doesNotContain("\n").doesNotContain(" ");
    }
}
