package com.boulder.boulder.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.common.base.Ticker;

/**
 * Frena la fuerza bruta sobre el inicio de sesión: tras cinco fallos seguidos
 * la cuenta queda bloqueada quince minutos desde el último fallo.
 */
class LimiteIntentosLoginTest {

    /** Reloj manual para probar la expiración sin esperar. */
    static class RelojManual extends Ticker {
        private final AtomicLong nanos = new AtomicLong();

        @Override
        public long read() {
            return nanos.get();
        }

        void avanzar(Duration d) {
            nanos.addAndGet(d.toNanos());
        }
    }

    private RelojManual reloj;
    private LimiteIntentosLogin limite;

    @BeforeEach
    void preparar() {
        reloj = new RelojManual();
        limite = new LimiteIntentosLogin(5, Duration.ofMinutes(15), reloj);
    }

    private void fallar(String usuario, int veces) {
        for (int i = 0; i < veces; i++) {
            limite.registrarFallo(usuario);
        }
    }

    @Test
    @DisplayName("Una cuenta sin fallos no está bloqueada")
    void sinFallos() {
        assertThat(limite.bloqueado("jvelazco")).isFalse();
    }

    @Test
    @DisplayName("Cuatro fallos se toleran; el quinto bloquea")
    void bloqueaAlQuinto() {
        fallar("jvelazco", 4);
        assertThat(limite.bloqueado("jvelazco")).isFalse();
        limite.registrarFallo("jvelazco");
        assertThat(limite.bloqueado("jvelazco")).isTrue();
    }

    @Test
    @DisplayName("Un inicio de sesión correcto borra los fallos acumulados")
    void exitoLimpia() {
        fallar("jvelazco", 4);
        limite.registrarExito("jvelazco");
        fallar("jvelazco", 4);
        assertThat(limite.bloqueado("jvelazco")).isFalse();
    }

    @Test
    @DisplayName("El usuario se compara sin mayúsculas ni espacios")
    void claveNormalizada() {
        fallar(" JVelazco ", 5);
        assertThat(limite.bloqueado("jvelazco")).isTrue();
    }

    @Test
    @DisplayName("El bloqueo vence quince minutos después del último fallo")
    void vence() {
        fallar("jvelazco", 5);
        reloj.avanzar(Duration.ofMinutes(14));
        assertThat(limite.bloqueado("jvelazco")).isTrue();
        reloj.avanzar(Duration.ofMinutes(1).plusSeconds(1));
        assertThat(limite.bloqueado("jvelazco")).isFalse();
    }

    @Test
    @DisplayName("Los fallos de un usuario no bloquean a otro")
    void independientes() {
        fallar("jvelazco", 5);
        assertThat(limite.bloqueado("agomez")).isFalse();
    }

    @Test
    @DisplayName("La configuración se valida al crear el límite")
    void configuracionValida() {
        assertThatThrownBy(() -> new LimiteIntentosLogin(0, Duration.ofMinutes(15), reloj))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new LimiteIntentosLogin(5, Duration.ZERO, reloj))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
