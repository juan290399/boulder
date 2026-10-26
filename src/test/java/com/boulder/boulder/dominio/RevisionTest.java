package com.boulder.boulder.dominio;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Las comprobaciones que comparten los siete partes de testigo. */
class RevisionTest {

    private static BigDecimal m(String metros) {
        return new BigDecimal(metros);
    }

    @Test
    @DisplayName("Un parte sin nada que objetar es válido")
    void valida() {
        Revision r = new Revision()
                .tramo("tramo", m("10"), m("12.5"))
                .cajas("cajas", 4, 6)
                .avance(m("10"), m("12.5"), m("2.50"))
                .opcion("turno", "dia", "dia", "noche")
                .porcentaje("pct_completado", m("100"));
        assertThat(r.valida()).isTrue();
        assertThat(r.errores()).isEmpty();
    }

    @Test
    @DisplayName("Los campos opcionales vacíos no se objetan")
    void opcionalesVacios() {
        Revision r = new Revision()
                .tramo("tramo", null, m("12"))
                .cajas("cajas", 4, null)
                .avance(m("10"), m("12"), null)
                .opcion("seco_humedo", null, "seco", "humedo")
                .porcentaje("pct", null);
        assertThat(r.errores()).isEmpty();
    }

    @Test
    @DisplayName("Un tramo invertido se objeta con su nombre")
    void tramoInvertido() {
        assertThat(new Revision().tramo("marcado", m("40"), m("35")).errores())
                .singleElement().asString().contains("marcado").contains("40").contains("35");
    }

    @Test
    @DisplayName("Un rango de cajas invertido se objeta")
    void cajasInvertidas() {
        assertThat(new Revision().cajas("cajas", 18, 12).errores())
                .singleElement().asString().contains("18").contains("12");
    }

    @Test
    @DisplayName("Un avance que no es hasta menos desde se objeta con la resta correcta")
    void avance() {
        assertThat(new Revision().avance(m("10"), m("12.5"), m("3")).errores())
                .singleElement().asString().contains("2.5");
    }

    @Test
    @DisplayName("Un valor fuera de la lista se objeta mostrando la lista")
    void opcion() {
        assertThat(new Revision().opcion("etapa", "horno", "corte", "secado_1").errores())
                .singleElement().asString().contains("horno").contains("corte, secado_1");
    }

    @Test
    @DisplayName("Un porcentaje fuera de 0 a 100 se objeta")
    void porcentaje() {
        assertThat(new Revision().porcentaje("pct", m("120")).errores()).hasSize(1);
        assertThat(new Revision().porcentaje("pct", m("-1")).errores()).hasSize(1);
    }

    @Test
    @DisplayName("Se juntan todos los motivos, no solo el primero")
    void todos() {
        Revision r = new Revision()
                .tramo("tramo", m("5"), m("1"))
                .opcion("turno", "tarde", "dia", "noche");
        assertThat(r.valida()).isFalse();
        assertThat(r.errores()).hasSize(2);
    }
}
