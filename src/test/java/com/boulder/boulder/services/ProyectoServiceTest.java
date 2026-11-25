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

import com.boulder.boulder.dto.ProyectoDTO;
import com.boulder.boulder.entities.Empresa;
import com.boulder.boulder.entities.Proyecto;
import com.boulder.boulder.repositories.EmpresaRepository;
import com.boulder.boulder.repositories.ProyectoRepository;

@ExtendWith(MockitoExtension.class)
class ProyectoServiceTest {

    @Mock
    private ProyectoRepository proyectos;

    @Mock
    private EmpresaRepository empresas;

    @InjectMocks
    private ProyectoServiceImpl servicio;

    private final Empresa empresa = new Empresa();

    @BeforeEach
    void preparar() {
        empresa.setId(UUID.randomUUID());
        lenient().when(empresas.findById(empresa.getId())).thenReturn(Optional.of(empresa));
        lenient().when(proyectos.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    private ProyectoDTO datos() {
        ProyectoDTO dto = new ProyectoDTO();
        dto.setCodigo("ver");
        dto.setNombre("Veronica");
        dto.setEmpresaId(empresa.getId());
        dto.setSrid(32718);
        dto.setUbicacion("Ayacucho - Parinacochas - Pullo");
        return dto;
    }

    @Test
    @DisplayName("El alta guarda el proyecto con el código en mayúsculas")
    void alta() {
        servicio.guardar(datos());

        ArgumentCaptor<Proyecto> guardado = ArgumentCaptor.forClass(Proyecto.class);
        verify(proyectos).save(guardado.capture());
        assertThat(guardado.getValue().getCodigo()).isEqualTo("VER");
        assertThat(guardado.getValue().getEmpresa()).isSameAs(empresa);
    }

    @Test
    @DisplayName("El alta rechaza un proyecto sin empresa, sin nombre o con un código que no tiene 3 letras")
    void altaValida() {
        ProyectoDTO dto = datos();
        dto.setCodigo("VERO");
        dto.setNombre(" ");
        dto.setEmpresaId(null);

        assertThatThrownBy(() -> servicio.guardar(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("3 letras").hasMessageContaining("nombre").hasMessageContaining("empresa");
        verify(proyectos, never()).save(any());
    }

    @Test
    @DisplayName("El alta rechaza un SRID que no es una zona UTM del Perú")
    void sridFueraDelPeru() {
        ProyectoDTO dto = datos();
        dto.setSrid(4326);

        assertThatThrownBy(() -> servicio.guardar(dto))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("SRID");
    }

    @Test
    @DisplayName("El alta rechaza un código ya usado por otro proyecto")
    void altaDuplicada() {
        when(proyectos.existsByCodigo("VER")).thenReturn(true);

        assertThatThrownBy(() -> servicio.guardar(datos()))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("VER");
        verify(proyectos, never()).save(any());
    }

    @Test
    @DisplayName("Editar no acepta datos vacíos y conserva el código del proyecto")
    void edicion() {
        UUID id = UUID.randomUUID();
        Proyecto actual = new Proyecto();
        actual.setId(id);
        actual.setCodigo("VER");
        actual.setEmpresa(empresa);
        when(proyectos.findById(id)).thenReturn(Optional.of(actual));

        ProyectoDTO vacio = datos();
        vacio.setNombre("");
        assertThatThrownBy(() -> servicio.actualizar(id, vacio))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("nombre");

        ProyectoDTO dto = datos();
        dto.setCodigo("OTR");
        dto.setNombre("Veronica Norte");
        servicio.actualizar(id, dto);
        assertThat(actual.getCodigo()).isEqualTo("VER");
        assertThat(actual.getNombre()).isEqualTo("Veronica Norte");
    }
}
