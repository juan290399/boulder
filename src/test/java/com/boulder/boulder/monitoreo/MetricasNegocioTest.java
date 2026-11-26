package com.boulder.boulder.monitoreo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.boulder.boulder.eventos.CambioDatos;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class MetricasNegocioTest {

    private SimpleMeterRegistry registro;
    private MetricasNegocio metricas;

    @BeforeEach
    void preparar() {
        registro = new SimpleMeterRegistry();
        metricas = new MetricasNegocio(registro);
    }

    @Test
    @DisplayName("Cada cambio confirmado suma sus filas por recurso y origen")
    void filasPorRecursoYOrigen() {
        metricas.alCambiar(CambioDatos.de("quicklog", Set.of(UUID.randomUUID()), "tablet", 3));
        metricas.alCambiar(CambioDatos.de("quicklog", Set.of(), "tablet", 2));
        metricas.alCambiar(CambioDatos.de("foto", Set.of(), "web", 1));

        assertThat(registro.get(MetricasNegocio.FILAS).tags("recurso", "quicklog", "origen", "tablet").counter().count())
                .isEqualTo(5.0);
        assertThat(registro.get(MetricasNegocio.FILAS).tags("recurso", "foto", "origen", "web").counter().count())
                .isEqualTo(1.0);
    }

    @Test
    @DisplayName("Los inicios de sesión se cuentan por resultado")
    void loginPorResultado() {
        metricas.login(MetricasNegocio.ResultadoLogin.CORRECTO);
        metricas.login(MetricasNegocio.ResultadoLogin.FALLIDO);
        metricas.login(MetricasNegocio.ResultadoLogin.FALLIDO);
        metricas.login(MetricasNegocio.ResultadoLogin.BLOQUEADO);

        assertThat(registro.get(MetricasNegocio.LOGIN).tag("resultado", "fallido").counter().count()).isEqualTo(2.0);
        assertThat(registro.get(MetricasNegocio.LOGIN).tag("resultado", "correcto").counter().count()).isEqualTo(1.0);
        assertThat(registro.get(MetricasNegocio.LOGIN).tag("resultado", "bloqueado").counter().count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Las filas de logueo rechazadas se cuentan por plantilla; cero no crea la serie")
    void rechazosLogueo() {
        metricas.rechazosLogueo("quicklog", 2);
        metricas.rechazosLogueo("litologia", 0);

        assertThat(registro.get(MetricasNegocio.RECHAZOS).tag("plantilla", "quicklog").counter().count()).isEqualTo(2.0);
        assertThat(registro.find(MetricasNegocio.RECHAZOS).tag("plantilla", "litologia").counter()).isNull();
    }
}
