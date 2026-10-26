package com.boulder.boulder.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.boulder.boulder.entities.Usuario;
import com.boulder.boulder.repositories.UsuarioRepository;

/**
 * Cambio de contraseña del propio usuario (RF02, CU02). El repositorio es un
 * doble de prueba: el servicio no sabe ni le importa que detrás haya JPA.
 */
@ExtendWith(MockitoExtension.class)
class CambioContrasenaServiceTest {

    @Mock
    private UsuarioRepository usuarios;

    /** BCrypt real con costo bajo: lo que importa es que el hash sea de verdad. */
    private final PasswordEncoder codificador = new BCryptPasswordEncoder(4);

    private CambioContrasenaService servicio;
    private Usuario cuenta;

    @BeforeEach
    void preparar() {
        servicio = new CambioContrasenaServiceImpl(usuarios, codificador);
        cuenta = new Usuario();
        cuenta.setUsuario("jvelazco");
        cuenta.setContrasenia(codificador.encode("Actual2026"));
    }

    @Test
    @DisplayName("Con la contraseña actual correcta se guarda la nueva cifrada")
    void cambia() {
        when(usuarios.findByUsuario("jvelazco")).thenReturn(Optional.of(cuenta));

        servicio.cambiar("jvelazco", "Actual2026", "Testigo2027");

        ArgumentCaptor<Usuario> guardado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarios).save(guardado.capture());
        String hash = guardado.getValue().getContrasenia();
        assertThat(hash).isNotEqualTo("Testigo2027");
        assertThat(codificador.matches("Testigo2027", hash)).isTrue();
    }

    @Test
    @DisplayName("Si la contraseña actual no coincide no se cambia nada")
    void actualIncorrecta() {
        when(usuarios.findByUsuario("jvelazco")).thenReturn(Optional.of(cuenta));

        assertThatThrownBy(() -> servicio.cambiar("jvelazco", "otra", "Testigo2027"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("actual");
        verify(usuarios, never()).save(any());
    }

    @ParameterizedTest(name = "«{0}» se rechaza")
    @ValueSource(strings = {"corta1", "sololetras", "1234567890", "jvelazco2026"})
    @DisplayName("La nueva debe tener 8 caracteres, letras y números, y no contener el usuario")
    void debil(String nueva) {
        when(usuarios.findByUsuario("jvelazco")).thenReturn(Optional.of(cuenta));

        assertThatThrownBy(() -> servicio.cambiar("jvelazco", "Actual2026", nueva))
                .isInstanceOf(IllegalArgumentException.class);
        verify(usuarios, never()).save(any());
    }

    @Test
    @DisplayName("La nueva no puede ser igual a la actual")
    void igualALaActual() {
        when(usuarios.findByUsuario("jvelazco")).thenReturn(Optional.of(cuenta));

        assertThatThrownBy(() -> servicio.cambiar("jvelazco", "Actual2026", "Actual2026"))
                .isInstanceOf(IllegalArgumentException.class);
        verify(usuarios, never()).save(any());
    }

    @Test
    @DisplayName("Un usuario inexistente no revela que no existe")
    void inexistente() {
        when(usuarios.findByUsuario("nadie")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.cambiar("nadie", "x", "Testigo2027"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("actual");
    }
}
