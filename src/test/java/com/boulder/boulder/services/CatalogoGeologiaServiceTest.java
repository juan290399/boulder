package com.boulder.boulder.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.boulder.boulder.dto.CatalogoGeologiaDTO;
import com.boulder.boulder.repositories.CatalogoGeologiaRepository;
import com.boulder.boulder.repositories.CatalogoGeologiaRepository.Estructura;

import jakarta.persistence.EntityNotFoundException;

/**
 * Mantenimiento de los diccionarios geológicos (RF04, CU06). El repositorio es un
 * mock que describe tres catálogos con formas distintas, como los reales.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CatalogoGeologiaServiceTest {

    @Mock
    private CatalogoGeologiaRepository repositorio;

    private CatalogoGeologiaService servicio;

    @BeforeEach
    void preparar() {
        when(repositorio.estructura()).thenReturn(Map.of(
                "lit_diccionario", new Estructura(Set.of("grupo_id", "color_r", "color_g", "color_b", "esta_activo"), 4),
                "alt_intensidad", new Estructura(Set.of(), 4),
                "min_zona", new Estructura(Set.of("esta_activo"), 3)));
        when(repositorio.insertar(anyString(), any())).thenReturn(UUID.randomUUID());
        servicio = new CatalogoGeologiaServiceImpl(repositorio);
    }

    private static CatalogoGeologiaDTO entrada(String codigo, String nombre) {
        CatalogoGeologiaDTO d = new CatalogoGeologiaDTO();
        d.setCodigo(codigo);
        d.setNombre(nombre);
        return d;
    }

    @Test
    @DisplayName("El código se guarda recortado y en mayúsculas")
    void normalizaCodigo() {
        servicio.crear("alt_intensidad", entrada("  mo ", "Moderada"));

        ArgumentCaptor<CatalogoGeologiaDTO> guardado = ArgumentCaptor.forClass(CatalogoGeologiaDTO.class);
        verify(repositorio).insertar(eq("alt_intensidad"), guardado.capture());
        assertThat(guardado.getValue().getCodigo()).isEqualTo("MO");
    }

    @Test
    @DisplayName("Un catálogo que no existe no llega a la base: es un nombre de tabla")
    void catalogoDesconocido() {
        assertThatThrownBy(() -> servicio.crear("usuario; DROP TABLE x", entrada("A", "B")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Catálogo");
        verify(repositorio, never()).insertar(anyString(), any());
    }

    @ParameterizedTest(name = "código «{0}» se rechaza")
    @ValueSource(strings = {"", "  ", "AB C", "AB-1", "ABCDE"})
    @DisplayName("El código es alfanumérico y cabe en la columna")
    void codigoInvalido(String codigo) {
        assertThatThrownBy(() -> servicio.crear("alt_intensidad", entrada(codigo, "Algo")))
                .isInstanceOf(IllegalArgumentException.class);
        verify(repositorio, never()).insertar(anyString(), any());
    }

    @Test
    @DisplayName("El nombre es obligatorio")
    void sinNombre() {
        assertThatThrownBy(() -> servicio.crear("alt_intensidad", entrada("MO", " ")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Una litología necesita su grupo litológico")
    void litologiaSinGrupo() {
        assertThatThrownBy(() -> servicio.crear("lit_diccionario", entrada("MZT", "Monzonita")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("grupo");
    }

    @Test
    @DisplayName("El color va completo y cada canal entre 0 y 255")
    void color() {
        CatalogoGeologiaDTO d = entrada("MZT", "Monzonita");
        d.setGrupoId(UUID.randomUUID());
        d.setColorR((short) 180);
        assertThatThrownBy(() -> servicio.crear("lit_diccionario", d)).isInstanceOf(IllegalArgumentException.class);

        d.setColorG((short) 150);
        d.setColorB((short) 300);
        assertThatThrownBy(() -> servicio.crear("lit_diccionario", d)).isInstanceOf(IllegalArgumentException.class);

        d.setColorB((short) 130);
        servicio.crear("lit_diccionario", d);
        verify(repositorio).insertar(eq("lit_diccionario"), any());
    }

    @Test
    @DisplayName("Un catálogo sin color no acepta color")
    void colorDondeNoHay() {
        CatalogoGeologiaDTO d = entrada("MO", "Moderada");
        d.setColorR((short) 1);
        d.setColorG((short) 2);
        d.setColorB((short) 3);
        assertThatThrownBy(() -> servicio.crear("alt_intensidad", d)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Solo se desactiva donde el catálogo lo admite")
    void desactivar() {
        UUID id = UUID.randomUUID();
        when(repositorio.cambiarEstado("min_zona", id, false)).thenReturn(1);
        servicio.cambiarEstado("min_zona", id, false);
        verify(repositorio).cambiarEstado("min_zona", id, false);

        assertThatThrownBy(() -> servicio.cambiarEstado("alt_intensidad", id, false))
                .isInstanceOf(IllegalStateException.class);
        verify(repositorio, never()).cambiarEstado(eq("alt_intensidad"), any(), anyBoolean());
    }

    @Test
    @DisplayName("Actualizar un código que no existe responde «no encontrado»")
    void actualizarInexistente() {
        UUID id = UUID.randomUUID();
        when(repositorio.actualizar(eq("alt_intensidad"), eq(id), any())).thenReturn(0);
        assertThatThrownBy(() -> servicio.actualizar("alt_intensidad", id, entrada("MO", "Moderada")))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
