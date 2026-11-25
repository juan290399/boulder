package com.boulder.boulder.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
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

import com.boulder.boulder.dto.SondajeDTO;
import com.boulder.boulder.entities.Collar;
import com.boulder.boulder.entities.Maquina;
import com.boulder.boulder.entities.Plataforma;
import com.boulder.boulder.entities.Proyecto;
import com.boulder.boulder.entities.Sondaje;
import com.boulder.boulder.repositories.CollarRepository;
import com.boulder.boulder.repositories.MaquinaRepository;
import com.boulder.boulder.repositories.PlataformaRepository;
import com.boulder.boulder.repositories.ProgramaSondajeRepository;
import com.boulder.boulder.repositories.ProyectoRepository;
import com.boulder.boulder.repositories.SondajeRepository;

@ExtendWith(MockitoExtension.class)
class SondajeServiceTest {

    @Mock private SondajeRepository sondajes;
    @Mock private ProyectoRepository proyectos;
    @Mock private ProgramaSondajeRepository programas;
    @Mock private CollarRepository collares;
    @Mock private PlataformaRepository plataformas;
    @Mock private MaquinaRepository maquinas;

    @InjectMocks
    private SondajeServiceImpl servicio;

    private final UUID proyectoId = UUID.randomUUID();
    private final UUID collarId = UUID.randomUUID();

    @BeforeEach
    void catalogos() {
        lenient().when(proyectos.findById(proyectoId)).thenReturn(Optional.of(new Proyecto()));
        lenient().when(collares.findById(collarId)).thenReturn(Optional.of(new Collar()));
        lenient().when(sondajes.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    private SondajeDTO datos() {
        SondajeDTO dto = new SondajeDTO();
        dto.setCodigo("DDH-VERO-S-046-26");
        dto.setNombre("DDH-046");
        dto.setProyectoId(proyectoId);
        dto.setCollarId(collarId);
        dto.setAzimutSetup(135.0);
        dto.setInclinacionSetup(-60.0);
        dto.setProfundidadActual(0.0);
        dto.setProfundidadFinal(400.0);
        dto.setFechaInicio(LocalDate.of(2026, 11, 1));
        return dto;
    }

    @Test
    @DisplayName("El alta guarda el código sin espacios y en mayúsculas")
    void altaNormalizaCodigo() {
        SondajeDTO dto = datos();
        dto.setCodigo("  ddh-vero-s-046-26 ");

        servicio.guardar(dto);

        ArgumentCaptor<Sondaje> guardado = ArgumentCaptor.forClass(Sondaje.class);
        verify(sondajes).save(guardado.capture());
        assertThat(guardado.getValue().getCodigo()).isEqualTo("DDH-VERO-S-046-26");
    }

    @Test
    @DisplayName("El alta dice qué falta: código, nombre corto, proyecto y collar")
    void altaPideObligatorios() {
        SondajeDTO dto = datos();
        dto.setCodigo(" ");
        dto.setNombre("");
        dto.setProyectoId(null);
        dto.setCollarId(null);

        assertThatThrownBy(() -> servicio.guardar(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("código").hasMessageContaining("nombre corto")
                .hasMessageContaining("proyecto").hasMessageContaining("collar");
        verify(sondajes, never()).save(any());
    }

    @Test
    @DisplayName("El alta rechaza azimut, inclinación, profundidades y fechas imposibles")
    void altaRechazaValoresImposibles() {
        SondajeDTO dto = datos();
        dto.setAzimutSetup(400.0);
        dto.setInclinacionSetup(-95.0);
        dto.setProfundidadFinal(-10.0);
        dto.setFechaFin(LocalDate.of(2026, 10, 1));

        assertThatThrownBy(() -> servicio.guardar(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("azimut").hasMessageContaining("inclinación")
                .hasMessageContaining("profundidad programada").hasMessageContaining("fecha de fin");
        verify(sondajes, never()).save(any());
    }

    @Test
    @DisplayName("El alta rechaza un código que ya existe, con un mensaje claro")
    void altaRechazaCodigoRepetido() {
        when(sondajes.existsByCodigo("DDH-VERO-S-046-26")).thenReturn(true);

        assertThatThrownBy(() -> servicio.guardar(datos()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe un sondaje con el código DDH-VERO-S-046-26");
        verify(sondajes, never()).save(any());
    }

    @Test
    @DisplayName("La edición puede quitar la plataforma y la máquina")
    void edicionQuitaPlataformaYMaquina() {
        UUID id = UUID.randomUUID();
        Sondaje existente = new Sondaje();
        existente.setCodigo("DDH-VERO-S-046-26");
        existente.setPlataforma(new Plataforma());
        existente.setMaquina(new Maquina());
        when(sondajes.findById(id)).thenReturn(Optional.of(existente));

        servicio.actualizar(id, datos());

        assertThat(existente.getPlataforma()).isNull();
        assertThat(existente.getMaquina()).isNull();
    }
}
