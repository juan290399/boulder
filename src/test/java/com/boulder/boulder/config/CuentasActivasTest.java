package com.boulder.boulder.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.boulder.boulder.entities.Usuario;
import com.boulder.boulder.repositories.UsuarioRepository;
import com.google.common.base.Ticker;

/** Un token vigente no basta: la cuenta tiene que seguir activa (RF03). */
@ExtendWith(MockitoExtension.class)
class CuentasActivasTest {

    /** Reloj que solo avanza cuando la prueba lo pide. */
    static class RelojManual extends Ticker {
        private long nanos;

        @Override
        public long read() {
            return nanos;
        }

        void avanzar(Duration d) {
            nanos += d.toNanos();
        }
    }

    @Mock
    private UsuarioRepository usuarios;

    private RelojManual reloj;
    private CuentasActivas cuentas;

    @BeforeEach
    void preparar() {
        reloj = new RelojManual();
        cuentas = new CuentasActivas(usuarios, Duration.ofSeconds(30), reloj);
    }

    private static Usuario cuenta(boolean activa) {
        Usuario u = new Usuario();
        u.setUsuario("jvelazco");
        u.setEstado(activa);
        return u;
    }

    @Test
    @DisplayName("Una cuenta activa pasa")
    void activa() {
        when(usuarios.findByUsuario("jvelazco")).thenReturn(Optional.of(cuenta(true)));
        assertThat(cuentas.activa("jvelazco")).isTrue();
    }

    @Test
    @DisplayName("Una cuenta desactivada no pasa aunque su token siga vigente")
    void desactivada() {
        when(usuarios.findByUsuario("jvelazco")).thenReturn(Optional.of(cuenta(false)));
        assertThat(cuentas.activa("jvelazco")).isFalse();
    }

    @Test
    @DisplayName("Una cuenta borrada no pasa")
    void borrada() {
        when(usuarios.findByUsuario("prueba_ui")).thenReturn(Optional.empty());
        assertThat(cuentas.activa("prueba_ui")).isFalse();
    }

    @Test
    @DisplayName("Dentro de los 30 segundos no vuelve a consultar la base")
    void recuerda() {
        when(usuarios.findByUsuario("jvelazco")).thenReturn(Optional.of(cuenta(true)));
        cuentas.activa("jvelazco");
        reloj.avanzar(Duration.ofSeconds(29));
        cuentas.activa("jvelazco");
        verify(usuarios, times(1)).findByUsuario("jvelazco");
    }

    @Test
    @DisplayName("Pasados los 30 segundos relee la cuenta")
    void relee() {
        when(usuarios.findByUsuario("jvelazco"))
                .thenReturn(Optional.of(cuenta(true)), Optional.of(cuenta(false)));
        assertThat(cuentas.activa("jvelazco")).isTrue();
        reloj.avanzar(Duration.ofSeconds(31));
        assertThat(cuentas.activa("jvelazco")).isFalse();
    }

    @Test
    @DisplayName("Al desactivar la cuenta se olvida lo recordado y el corte es inmediato")
    void olvidar() {
        when(usuarios.findByUsuario("jvelazco"))
                .thenReturn(Optional.of(cuenta(true)), Optional.of(cuenta(false)));
        assertThat(cuentas.activa("jvelazco")).isTrue();
        cuentas.olvidar("jvelazco");
        assertThat(cuentas.activa("jvelazco")).isFalse();
    }
}
