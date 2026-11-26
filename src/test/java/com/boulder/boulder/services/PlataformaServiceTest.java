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

import com.boulder.boulder.dto.PlataformaDTO;
import com.boulder.boulder.entities.Plataforma;
import com.boulder.boulder.entities.Proyecto;
import com.boulder.boulder.repositories.PlataformaRepository;
import com.boulder.boulder.repositories.ProyectoRepository;

@ExtendWith(MockitoExtension.class)
class PlataformaServiceTest {

    @Mock private PlataformaRepository plataformas;
    @Mock private ProyectoRepository proyectos;

    @InjectMocks
    private PlataformaServiceImpl servicio;

    private final UUID proyectoId = UUID.randomUUID();
    private final Proyecto proyecto = new Proyecto();

    @BeforeEach
    void catalogos() {
        proyecto.setId(proyectoId);
        lenient().when(proyectos.findById(proyectoId)).thenReturn(Optional.of(proyecto));
        lenient().when(plataformas.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    private PlataformaDTO datos() {
        PlataformaDTO dto = new PlataformaDTO();
        dto.setProyectoId(proyectoId);
        dto.setCodigo("PL-046");
        dto.setNombre("Plataforma 46");
        dto.setEste(285430.5);
        dto.setNorte(8432110.2);
        dto.setElevacion(4325.0);
        dto.setNumeroPozas((short) 2);
        dto.setEstado("Operativo");
        dto.setTipo("Superficie");
        dto.setUbicacion("Ladera norte");
        dto.setNotas("Acceso por trocha 3");
        return dto;
    }

    @Test
    @DisplayName("El alta guarda coordenadas, tipo, ubicación, pozas y notas, con el código en mayúsculas")
    void altaGuardaTodo() {
        PlataformaDTO dto = datos();
        dto.setCodigo(" pl-046 ");

        servicio.guardar(dto);

        ArgumentCaptor<Plataforma> guardada = ArgumentCaptor.forClass(Plataforma.class);
        verify(plataformas).save(guardada.capture());
        Plataforma p = guardada.getValue();
        assertThat(p.getCodigo()).isEqualTo("PL-046");
        assertThat(p.getEste()).isEqualTo(285430.5);
        assertThat(p.getNorte()).isEqualTo(8432110.2);
        assertThat(p.getElevacion()).isEqualTo(4325.0);
        assertThat(p.getNumeroPozas()).isEqualTo((short) 2);
        assertThat(p.getTipo()).isEqualTo("Superficie");
        assertThat(p.getUbicacion()).isEqualTo("Ladera norte");
        assertThat(p.getNotas()).isEqualTo("Acceso por trocha 3");
    }

    @Test
    @DisplayName("El alta dice qué falta: código, nombre, proyecto y coordenadas")
    void altaPideObligatorios() {
        PlataformaDTO dto = new PlataformaDTO();

        assertThatThrownBy(() -> servicio.guardar(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("código").hasMessageContaining("nombre")
                .hasMessageContaining("proyecto").hasMessageContaining("este")
                .hasMessageContaining("norte").hasMessageContaining("elevación");
        verify(plataformas, never()).save(any());
    }

    @Test
    @DisplayName("El alta rechaza un código de más de 8 caracteres, pozas negativas o un código repetido")
    void altaRechazaValoresInvalidos() {
        PlataformaDTO largo = datos();
        largo.setCodigo("PLATAFORMA-46");
        largo.setNumeroPozas((short) -1);
        assertThatThrownBy(() -> servicio.guardar(largo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("8 caracteres").hasMessageContaining("pozas");

        when(plataformas.existsByCodigo("PL-046")).thenReturn(true);
        assertThatThrownBy(() -> servicio.guardar(datos()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe una plataforma con el código PL-046");
        verify(plataformas, never()).save(any());
    }

    @Test
    @DisplayName("El alta rechaza coordenadas fuera del rango UTM del hemisferio sur")
    void altaRechazaCoordenadasFueraDeRango() {
        PlataformaDTO dto = datos();
        dto.setEste(12.0);
        dto.setNorte(12_000_000.0);

        assertThatThrownBy(() -> servicio.guardar(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("este debe estar entre 160 000 y 840 000")
                .hasMessageContaining("norte debe estar entre 0 y 10 000 000");
        verify(plataformas, never()).save(any());
    }

    @Test
    @DisplayName("La edición guarda coordenadas, tipo, ubicación y pozas (antes se perdían)")
    void edicionGuardaTodo() {
        UUID id = UUID.randomUUID();
        Plataforma existente = new Plataforma();
        existente.setCodigo("PL-046");
        existente.setProyecto(proyecto);
        when(plataformas.findById(id)).thenReturn(Optional.of(existente));

        servicio.actualizar(id, datos());

        assertThat(existente.getEste()).isEqualTo(285430.5);
        assertThat(existente.getElevacion()).isEqualTo(4325.0);
        assertThat(existente.getTipo()).isEqualTo("Superficie");
        assertThat(existente.getNumeroPozas()).isEqualTo((short) 2);
    }

    @Test
    @DisplayName("La lectura devuelve coordenadas, tipo, ubicación y pozas")
    void lecturaDevuelveTodo() {
        UUID id = UUID.randomUUID();
        Plataforma p = new Plataforma();
        p.setId(id);
        p.setCodigo("PL-046");
        p.setProyecto(proyecto);
        p.setEste(285430.5);
        p.setNorte(8432110.2);
        p.setElevacion(4325.0);
        p.setTipo("Superficie");
        p.setUbicacion("Ladera norte");
        p.setNumeroPozas((short) 2);
        when(plataformas.findById(id)).thenReturn(Optional.of(p));

        PlataformaDTO dto = servicio.obtenerPorId(id);

        assertThat(dto.getEste()).isEqualTo(285430.5);
        assertThat(dto.getNorte()).isEqualTo(8432110.2);
        assertThat(dto.getElevacion()).isEqualTo(4325.0);
        assertThat(dto.getTipo()).isEqualTo("Superficie");
        assertThat(dto.getUbicacion()).isEqualTo("Ladera norte");
        assertThat(dto.getNumeroPozas()).isEqualTo((short) 2);
    }
}
