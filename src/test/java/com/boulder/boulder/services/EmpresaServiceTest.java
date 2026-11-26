package com.boulder.boulder.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.boulder.boulder.dto.EmpresaDTO;
import com.boulder.boulder.entities.Empresa;
import com.boulder.boulder.repositories.EmpresaRepository;

@ExtendWith(MockitoExtension.class)
class EmpresaServiceTest {

    @Mock
    private EmpresaRepository empresas;

    @InjectMocks
    private EmpresaServiceImpl servicio;

    private static EmpresaDTO datos() {
        EmpresaDTO dto = new EmpresaDTO();
        dto.setCodigo("GEOPERU");
        dto.setNombre("Geo Perú S.A.C.");
        dto.setRuc("20123456789");
        dto.setDireccion("Av. Ejército 101, Arequipa");
        dto.setTelefono("054 123456");
        dto.setCorreo("contacto@geoperu.pe");
        return dto;
    }

    @Test
    @DisplayName("El alta guarda todos los campos, incluidos el código y el correo")
    void altaGuardaTodo() {
        when(empresas.save(any())).thenAnswer(i -> i.getArgument(0));

        servicio.guardar(datos());

        ArgumentCaptor<Empresa> guardada = ArgumentCaptor.forClass(Empresa.class);
        verify(empresas).save(guardada.capture());
        assertThat(guardada.getValue().getCodigo()).isEqualTo("GEOPERU");
        assertThat(guardada.getValue().getRuc()).isEqualTo("20123456789");
        assertThat(guardada.getValue().getCorreo()).isEqualTo("contacto@geoperu.pe");
    }

    @Test
    @DisplayName("El alta rechaza una empresa sin código, sin razón social o con un RUC que no tiene 11 dígitos")
    void altaValida() {
        EmpresaDTO dto = datos();
        dto.setCodigo(" ");
        dto.setNombre("");
        dto.setRuc("2012345");

        assertThatThrownBy(() -> servicio.guardar(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("código").hasMessageContaining("razón social").hasMessageContaining("11 dígitos");
        verify(empresas, never()).save(any());
    }

    @Test
    @DisplayName("El alta rechaza un código o un RUC ya registrados")
    void altaDuplicada() {
        when(empresas.existsByCodigo("GEOPERU")).thenReturn(true);

        assertThatThrownBy(() -> servicio.guardar(datos()))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("GEOPERU");

        when(empresas.existsByCodigo("GEOPERU")).thenReturn(false);
        Empresa otra = new Empresa();
        otra.setId(UUID.randomUUID());
        when(empresas.findByRuc("20123456789")).thenReturn(Optional.of(otra));

        assertThatThrownBy(() -> servicio.guardar(datos()))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("20123456789");
        verify(empresas, never()).save(any());
    }

    @Test
    @DisplayName("Editar guarda el código, el RUC y el correo, no solo el nombre")
    void edicionGuardaTodo() {
        UUID id = UUID.randomUUID();
        Empresa actual = new Empresa();
        actual.setId(id);
        actual.setCodigo("VIEJO");
        actual.setNombre("Vieja S.A.C.");
        actual.setRuc("20999999999");
        when(empresas.findById(id)).thenReturn(Optional.of(actual));
        when(empresas.save(any())).thenAnswer(i -> i.getArgument(0));

        servicio.actualizar(id, datos());

        assertThat(actual.getCodigo()).isEqualTo("GEOPERU");
        assertThat(actual.getRuc()).isEqualTo("20123456789");
        assertThat(actual.getCorreo()).isEqualTo("contacto@geoperu.pe");
        assertThat(actual.getNombre()).isEqualTo("Geo Perú S.A.C.");
    }
}
