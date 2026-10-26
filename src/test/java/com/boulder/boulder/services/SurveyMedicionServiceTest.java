package com.boulder.boulder.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.boulder.boulder.dto.SurveyMedicionDTO;
import com.boulder.boulder.entities.Sondaje;
import com.boulder.boulder.entities.SurveyMedicion;
import com.boulder.boulder.repositories.EmpleadoRepository;
import com.boulder.boulder.repositories.MaquinaRepository;
import com.boulder.boulder.repositories.SondajeRepository;
import com.boulder.boulder.repositories.SurveyMedicionRepository;

/**
 * Registro de una medición de desvío desde el formulario (RF07, CU17). Los
 * repositorios son dobles de prueba.
 */
@ExtendWith(MockitoExtension.class)
class SurveyMedicionServiceTest {

    @Mock private SurveyMedicionRepository mediciones;
    @Mock private SondajeRepository sondajes;
    @Mock private MaquinaRepository maquinas;
    @Mock private EmpleadoRepository empleados;

    private SurveyMedicionService servicio;
    private final UUID sondajeId = UUID.randomUUID();

    @BeforeEach
    void preparar() {
        servicio = new SurveyMedicionServiceImpl(mediciones, sondajes, maquinas, empleados);
        Sondaje sondaje = new Sondaje();
        sondaje.setId(sondajeId);
        sondaje.setCodigo("DDH-VERO-S-041-26");
        sondaje.setAzimutSetup(299.0);
        sondaje.setInclinacionSetup(-60.0);
        lenient().when(sondajes.findById(sondajeId)).thenReturn(Optional.of(sondaje));
        lenient().when(mediciones.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    /** Una estación medida, sin plan: el plan sale del setup del sondaje. */
    private SurveyMedicionDTO medicion(String profundidad, Double azimut, Double inclinacion) {
        SurveyMedicionDTO dto = new SurveyMedicionDTO();
        dto.setSondajeId(sondajeId);
        dto.setProfundidad(new BigDecimal(profundidad));
        dto.setFechaMedicion(LocalDate.of(2026, 10, 26));
        dto.setAzimutMedido(azimut);
        dto.setInclinacionMedida(inclinacion);
        return dto;
    }

    private SurveyMedicion guardada() {
        ArgumentCaptor<SurveyMedicion> captor = ArgumentCaptor.forClass(SurveyMedicion.class);
        verify(mediciones).save(captor.capture());
        return captor.getValue();
    }

    private SurveyMedicion existente(String profundidad, short nro) {
        SurveyMedicion m = new SurveyMedicion();
        m.setId(UUID.randomUUID());
        m.setProfundidad(new BigDecimal(profundidad));
        m.setNroMedicion(nro);
        return m;
    }

    @Test
    @DisplayName("Sin plan explícito, el desvío se calcula contra el setup del sondaje")
    void calculaContraElSetup() {
        servicio.guardar(medicion("100", 300.5, -58.0));

        SurveyMedicion m = guardada();
        assertThat(m.getAzimutPlan()).isEqualTo(299.0);
        assertThat(m.getInclinacionPlan()).isEqualTo(-60.0);
        assertThat(m.getDesvAzimut()).isCloseTo(1.5, within(1e-4));
        assertThat(m.getDesvAzimutSentido()).isEqualTo("derecha");
        assertThat(m.getDesvInclinacion()).isCloseTo(2.0, within(1e-4));
        assertThat(m.getDesvInclinacionSentido()).isEqualTo("arriba");
        assertThat(m.getDesvTotal()).isPositive();
        assertThat(m.getDesvMetros()).isPositive();
    }

    @Test
    @DisplayName("Si se manda el plan, se usa ese y no el del setup")
    void planExplicito() {
        SurveyMedicionDTO dto = medicion("100", 297.0, -60.0);
        dto.setAzimutPlan(297.0);
        dto.setInclinacionPlan(-60.0);
        servicio.guardar(dto);

        assertThat(guardada().getDesvAzimutSentido()).isNull();
    }

    @Test
    @DisplayName("Sin azimut ni inclinación medidos, se respeta el desvío escrito a mano (partes viejos)")
    void desvioManual() {
        SurveyMedicionDTO dto = medicion("25", null, null);
        dto.setDesvAzimut(0.31);
        dto.setDesvAzimutSentido("izquierda");
        dto.setDesvMetros(0.05);
        servicio.guardar(dto);

        SurveyMedicion m = guardada();
        assertThat(m.getDesvAzimut()).isEqualTo(0.31);
        assertThat(m.getDesvAzimutSentido()).isEqualTo("izquierda");
        assertThat(m.getDesvMetros()).isEqualTo(0.05);
    }

    @Test
    @DisplayName("Azimut fuera de 0-360 e inclinación fuera de ±90 se rechazan juntos")
    void angulosFueraDeRango() {
        assertThatThrownBy(() -> servicio.guardar(medicion("100", 365.0, -95.0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("azimut").hasMessageContaining("inclinacion");
        verify(mediciones, never()).save(any());
    }

    @Test
    @DisplayName("La decisión sale de su lista")
    void decision() {
        SurveyMedicionDTO dto = medicion("100", 299.0, -60.0);
        dto.setDecision("abandonar");
        assertThatThrownBy(() -> servicio.guardar(dto))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("continuar");
    }

    @Test
    @DisplayName("Sin número de medición, toma el siguiente a esa profundidad")
    void numeroSiguiente() {
        when(mediciones.findBySondajeIdOrderByProfundidadAscNroMedicionAsc(sondajeId))
                .thenReturn(List.of(existente("90", (short) 1), existente("100.00", (short) 1)));

        servicio.guardar(medicion("100", 299.0, -60.0));

        assertThat(guardada().getNroMedicion()).isEqualTo((short) 2);
    }

    @Test
    @DisplayName("La misma estación con el mismo número no se carga dos veces")
    void duplicada() {
        when(mediciones.findBySondajeIdOrderByProfundidadAscNroMedicionAsc(sondajeId))
                .thenReturn(List.of(existente("100", (short) 1)));
        SurveyMedicionDTO dto = medicion("100.0", 299.0, -60.0);
        dto.setNroMedicion((short) 1);

        assertThatThrownBy(() -> servicio.guardar(dto))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("100");
        verify(mediciones, never()).save(any());
    }
}
