package com.boulder.boulder.services.importacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

class CatalogosTest {

    private static final UUID S035 = UUID.randomUUID();
    private static final UUID S036 = UUID.randomUUID();

    private Catalogos catalogos;

    @BeforeEach
    void preparar() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        Map<String, UUID> sondajes = Map.of("DDH-VERO-S-035-26", S035, "DDH-VERO-S-036-26", S036);
        doAnswer(inv -> {
            RowCallbackHandler h = inv.getArgument(1);
            for (var e : sondajes.entrySet()) {
                ResultSet rs = mock(ResultSet.class);
                when(rs.getString("clave")).thenReturn(e.getKey());
                when(rs.getObject("id")).thenReturn(e.getValue());
                h.processRow(rs);
            }
            return null;
        }).when(jdbc).query(contains("prf_sondaje"), any(RowCallbackHandler.class));
        catalogos = new Catalogos(jdbc);
    }

    @Test
    @DisplayName("El código exacto resuelve y devuelve el código del catálogo")
    void exacto() {
        assertThat(catalogos.sondaje(" ddh-vero-s-035-26 ")).isEqualTo(S035);
        assertThat(catalogos.codigoSondaje(S035)).isEqualTo("DDH-VERO-S-035-26");
    }

    @ParameterizedTest
    @ValueSource(strings = {"VERO S 35_26", "Sondaje-vero-s-35-26", "DDH-VERO-S035-26", "vero-s-35-26"})
    @DisplayName("Una escritura inequívoca se normaliza con CodigoSondaje")
    void normalizado(String escrito) {
        assertThat(catalogos.sondaje(escrito)).isEqualTo(S035);
    }

    @ParameterizedTest
    @ValueSource(strings = {"DDH-35", "Sondaje 036", "VERO S 35", "DDH-VERO-S-040-26"})
    @DisplayName("Una escritura ambigua o inexistente no resuelve")
    void sinResolver(String escrito) {
        assertThat(catalogos.sondaje(escrito)).isNull();
    }
}
