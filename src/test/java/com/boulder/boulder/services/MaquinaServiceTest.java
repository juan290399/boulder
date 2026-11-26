package com.boulder.boulder.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.boulder.boulder.dto.MaquinaDTO;
import com.boulder.boulder.entities.Maquina;
import com.boulder.boulder.repositories.MaquinaRepository;

@ExtendWith(MockitoExtension.class)
class MaquinaServiceTest {

    @Mock private MaquinaRepository maquinas;

    @InjectMocks
    private MaquinaServiceImpl servicio;

    @BeforeEach
    void guardarDevuelveLoMismo() {
        lenient().when(maquinas.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    private static MaquinaDTO datos() {
        MaquinaDTO dto = new MaquinaDTO();
        dto.setCodigo("H-800");
        dto.setNombre("Superdrill H800");
        dto.setTipo("Diamantina");
        dto.setMarca("Maqpower");
        dto.setMotor("Cummins 6CTA");
        dto.setAlcancePq((short) 500);
        dto.setAlcanceHq((short) 900);
        dto.setAlcanceNq((short) 1200);
        dto.setEstado("Operativa");
        return dto;
    }

    @Test
    @DisplayName("El alta guarda motor y alcances PQ, HQ y NQ, con el código en mayúsculas")
    void altaGuardaTodo() {
        MaquinaDTO dto = datos();
        dto.setCodigo(" h-800 ");

        servicio.guardar(dto);

        ArgumentCaptor<Maquina> guardada = ArgumentCaptor.forClass(Maquina.class);
        verify(maquinas).save(guardada.capture());
        Maquina m = guardada.getValue();
        assertThat(m.getCodigo()).isEqualTo("H-800");
        assertThat(m.getMotor()).isEqualTo("Cummins 6CTA");
        assertThat(m.getAlcancePq()).isEqualTo((short) 500);
        assertThat(m.getAlcanceHq()).isEqualTo((short) 900);
        assertThat(m.getAlcanceNq()).isEqualTo((short) 1200);
    }

    @Test
    @DisplayName("El alta dice qué falta y rechaza códigos largos o alcances negativos")
    void altaValida() {
        MaquinaDTO vacia = new MaquinaDTO();
        assertThatThrownBy(() -> servicio.guardar(vacia))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("código").hasMessageContaining("nombre");

        MaquinaDTO mala = datos();
        mala.setCodigo("SUPERDRILL-800");
        mala.setAlcanceHq((short) -5);
        assertThatThrownBy(() -> servicio.guardar(mala))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("10 caracteres").hasMessageContaining("alcance");
        verify(maquinas, never()).save(any());
    }

    @Test
    @DisplayName("El alta rechaza un código que ya existe")
    void altaRechazaRepetido() {
        when(maquinas.existsByCodigo("H-800")).thenReturn(true);

        assertThatThrownBy(() -> servicio.guardar(datos()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe una máquina con el código H-800");
        verify(maquinas, never()).save(any());
    }

    @Test
    @DisplayName("La edición guarda motor y alcances (antes se perdían)")
    void edicionGuardaTodo() {
        UUID id = UUID.randomUUID();
        Maquina existente = new Maquina();
        existente.setCodigo("H-800");
        when(maquinas.findById(id)).thenReturn(Optional.of(existente));

        servicio.actualizar(id, datos());

        assertThat(existente.getMotor()).isEqualTo("Cummins 6CTA");
        assertThat(existente.getAlcancePq()).isEqualTo((short) 500);
        assertThat(existente.getAlcanceNq()).isEqualTo((short) 1200);
    }

    @Test
    @DisplayName("La lectura devuelve motor y alcances")
    void lecturaDevuelveTodo() {
        UUID id = UUID.randomUUID();
        Maquina m = new Maquina();
        m.setId(id);
        m.setCodigo("H-800");
        m.setMotor("Cummins 6CTA");
        m.setAlcancePq((short) 500);
        m.setAlcanceHq((short) 900);
        m.setAlcanceNq((short) 1200);
        when(maquinas.findById(id)).thenReturn(Optional.of(m));

        MaquinaDTO dto = servicio.obtenerPorId(id);

        assertThat(dto.getMotor()).isEqualTo("Cummins 6CTA");
        assertThat(dto.getAlcancePq()).isEqualTo((short) 500);
        assertThat(dto.getAlcanceHq()).isEqualTo((short) 900);
        assertThat(dto.getAlcanceNq()).isEqualTo((short) 1200);
    }
}
