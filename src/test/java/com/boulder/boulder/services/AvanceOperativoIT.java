package com.boulder.boulder.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.boulder.boulder.dto.AvanceDensidadDTO;
import com.boulder.boulder.dto.AvanceFotografiaDTO;
import com.boulder.boulder.dto.AvanceLogueoDTO;
import com.boulder.boulder.dto.AvanceMuestreoDTO;
import com.boulder.boulder.dto.AvanceOperativoDTO;
import com.boulder.boulder.dto.CorteMuestraDTO;
import com.boulder.boulder.dto.RecepcionCajaDTO;
import com.boulder.boulder.entities.Sondaje;
import com.boulder.boulder.repositories.SondajeRepository;

/**
 * Los partes de testigo que llegan desde el formulario, contra PostgreSQL.
 *
 * <p>Un error de tecleo tiene que volver como un motivo legible (400), no como
 * una violación de CHECK de la base ni, peor, guardarse.</p>
 */
@SpringBootTest
@Transactional
class AvanceOperativoIT {

    @Autowired private RecepcionCajaService recepcion;
    @Autowired private AvanceLogueoService logueo;
    @Autowired private AvanceFotografiaService fotografia;
    @Autowired private CorteMuestraService corte;
    @Autowired private AvanceMuestreoService muestreo;
    @Autowired private AvanceDensidadService densidad;
    @Autowired private SondajeRepository sondajes;

    private Sondaje sondaje;

    @BeforeEach
    void preparar() {
        sondaje = sondajes.findAll().stream().findFirst().orElseThrow();
    }

    /** Lo que manda el formulario: el sondaje por id, sin código de texto. */
    private <D extends AvanceOperativoDTO> D cabecera(D dto) {
        dto.setSondajeId(sondaje.getId());
        dto.setFecha(LocalDate.of(2026, 10, 26));
        dto.setTurno("dia");
        return dto;
    }

    private static BigDecimal m(String metros) {
        return new BigDecimal(metros);
    }

    private RecepcionCajaDTO recepcionValida() {
        RecepcionCajaDTO dto = cabecera(new RecepcionCajaDTO());
        dto.setResponsable("Prueba");
        dto.setBoxDesde(901);
        dto.setBoxHasta(903);
        dto.setTotalCajas(3);
        dto.setMarcadoDesde(m("9990"));
        dto.setMarcadoHasta(m("9999"));
        return dto;
    }

    @Test
    @DisplayName("Un parte válido vuelve con su número interno y el código del sondaje")
    void valido() {
        RecepcionCajaDTO creado = recepcion.guardar(recepcionValida());

        assertThat(creado.getNumeroInterno()).isNotNull();
        assertThat(creado.getSondajeCodigo()).isEqualTo(sondaje.getCodigo());
    }

    @Test
    @DisplayName("Cajas invertidas: motivo legible, no una violación de CHECK")
    void cajasInvertidas() {
        RecepcionCajaDTO dto = recepcionValida();
        dto.setBoxDesde(903);
        dto.setBoxHasta(901);
        assertThatThrownBy(() -> recepcion.guardar(dto))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("caja");
    }

    @Test
    @DisplayName("El turno es día o noche en todos los partes")
    void turno() {
        RecepcionCajaDTO dto = recepcionValida();
        dto.setTurno("tarde");
        assertThatThrownBy(() -> recepcion.guardar(dto))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("turno");
    }

    @Test
    @DisplayName("Logueo: el avance tiene que ser hasta menos desde")
    void logueoAvance() {
        AvanceLogueoDTO dto = cabecera(new AvanceLogueoDTO());
        dto.setTipoLogueo("geologico");
        dto.setDesde(m("9990"));
        dto.setHasta(m("9995"));
        dto.setAvance(m("7"));
        assertThatThrownBy(() -> logueo.guardar(dto))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("avance");
    }

    @Test
    @DisplayName("Logueo: el tipo sale de su lista")
    void logueoTipo() {
        AvanceLogueoDTO dto = cabecera(new AvanceLogueoDTO());
        dto.setTipoLogueo("litologico");
        dto.setDesde(m("9990"));
        dto.setHasta(m("9995"));
        dto.setAvance(m("5"));
        assertThatThrownBy(() -> logueo.guardar(dto))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("geologico");
    }

    @Test
    @DisplayName("Fotografía: la pasada es seco o húmedo")
    void fotografiaPasada() {
        AvanceFotografiaDTO dto = cabecera(new AvanceFotografiaDTO());
        dto.setSecoHumedo("mojado");
        assertThatThrownBy(() -> fotografia.guardar(dto))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("seco");
    }

    @Test
    @DisplayName("Corte: el tramo no termina antes de empezar")
    void corteTramo() {
        CorteMuestraDTO dto = cabecera(new CorteMuestraDTO());
        dto.setDesde(m("9995"));
        dto.setHasta(m("9990"));
        assertThatThrownBy(() -> corte.guardar(dto)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Muestreo: el estado sale de su lista")
    void muestreoEstado() {
        AvanceMuestreoDTO dto = cabecera(new AvanceMuestreoDTO());
        dto.setEstadoMuestreo("listo");
        assertThatThrownBy(() -> muestreo.guardar(dto))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("en_proceso");
    }

    @Test
    @DisplayName("Estufa: la etapa sale de su lista")
    void densidadEtapa() {
        AvanceDensidadDTO dto = cabecera(new AvanceDensidadDTO());
        dto.setEtapa("horno");
        dto.setNroMuestras(4);
        assertThatThrownBy(() -> densidad.guardar(dto))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("secado_1");
    }
}
