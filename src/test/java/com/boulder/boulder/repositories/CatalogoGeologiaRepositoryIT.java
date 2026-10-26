package com.boulder.boulder.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.boulder.boulder.dto.CatalogoGeologiaDTO;
import com.boulder.boulder.services.CatalogoGeologiaService;

/**
 * Prueba de integración del DAO de diccionarios contra la base PostgreSQL real.
 * Cada prueba corre en una transacción que se deshace al final: la base queda
 * como estaba.
 */
@SpringBootTest
@Transactional
class CatalogoGeologiaRepositoryIT {

    @Autowired
    private CatalogoGeologiaService servicio;

    @Autowired
    private CatalogoGeologiaRepository repositorio;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    @DisplayName("La estructura se lee de la base: 21 catálogos, con el largo real del código")
    void estructura() {
        assertThat(repositorio.estructura()).hasSize(21)
                .containsKeys("lit_diccionario", "alt_intensidad", "est_tipo_venilla");
        assertThat(repositorio.estructura().get("lit_diccionario").tiene("grupo_id")).isTrue();
        assertThat(repositorio.estructura().get("est_tipo_venilla").largoCodigo()).isEqualTo(5);
    }

    @Test
    @DisplayName("Una litología creada aparece en la descarga de catálogos y se puede desactivar")
    void cicloDeUnaLitologia() {
        UUID grupo = jdbc.queryForObject("SELECT id FROM geologia.grupo_litologico LIMIT 1", UUID.class);
        CatalogoGeologiaDTO nueva = new CatalogoGeologiaDTO();
        nueva.setCodigo("zzt");
        nueva.setNombre("Litología de prueba");
        nueva.setGrupoId(grupo);
        nueva.setColorR((short) 10);
        nueva.setColorG((short) 20);
        nueva.setColorB((short) 30);

        CatalogoGeologiaDTO creada = servicio.crear("lit_diccionario", nueva);

        assertThat(servicio.listarTodos())
                .anySatisfy(c -> {
                    assertThat(c.getId()).isEqualTo(creada.getId());
                    assertThat(c.getCodigo()).isEqualTo("ZZT");
                    assertThat(c.getTipo()).isEqualTo("lit_diccionario");
                });

        servicio.cambiarEstado("lit_diccionario", creada.getId(), false);
        Boolean activo = jdbc.queryForObject("SELECT esta_activo FROM geologia.lit_diccionario WHERE id = ?",
                Boolean.class, creada.getId());
        assertThat(activo).isFalse();
    }

    @Test
    @DisplayName("El repositorio también rechaza un nombre de tabla que no es un catálogo")
    void listaBlanca() {
        // Spring traduce las excepciones que salen de un @Repository a su propia
        // jerarquía (DataAccessException); la causa original se conserva.
        assertThatThrownBy(() -> repositorio.insertar("usuario", new CatalogoGeologiaDTO()))
                .hasRootCauseInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Catálogo desconocido");
    }
}
