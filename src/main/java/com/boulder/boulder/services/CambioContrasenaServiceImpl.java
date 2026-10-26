package com.boulder.boulder.services;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boulder.boulder.entities.Usuario;
import com.boulder.boulder.repositories.UsuarioRepository;

/**
 * Cambio de contraseña del propio usuario.
 *
 * <p>Está separado de {@link UsuarioService}, que administra cuentas ajenas: aquí
 * la persona cambia la suya y debe probar que conoce la actual. Las dependencias
 * entran por el constructor, así el servicio se prueba con un repositorio falso
 * sin levantar Spring.</p>
 */
@Service
public class CambioContrasenaServiceImpl implements CambioContrasenaService {

    private static final Logger AUDITORIA = LoggerFactory.getLogger("com.boulder.boulder.seguridad");
    private static final int LARGO_MINIMO = 8;

    /** Mismo mensaje si el usuario no existe o la contraseña no coincide: no se revela cuál de las dos. */
    private static final String ACTUAL_INCORRECTA = "La contraseña actual no es correcta.";

    private final UsuarioRepository usuarios;
    private final PasswordEncoder codificador;

    public CambioContrasenaServiceImpl(UsuarioRepository usuarios, PasswordEncoder codificador) {
        this.usuarios = usuarios;
        this.codificador = codificador;
    }

    /**
     * Cambia la contraseña si la actual coincide y la nueva cumple las reglas.
     *
     * @param usuario el usuario que inició sesión (sale del token, no del formulario)
     * @param actual  la contraseña actual, para confirmar que es la persona
     * @param nueva   la contraseña nueva
     * @throws IllegalArgumentException si la actual no coincide o la nueva no cumple las reglas
     */
    @Override
    @Transactional
    public void cambiar(String usuario, String actual, String nueva) {
        Usuario cuenta = usuarios.findByUsuario(usuario)
                .filter(u -> actual != null && codificador.matches(actual, u.getContrasenia()))
                .orElseThrow(() -> new IllegalArgumentException(ACTUAL_INCORRECTA));

        validarNueva(usuario, actual, nueva);

        cuenta.setContrasenia(codificador.encode(nueva));
        usuarios.save(cuenta);
        AUDITORIA.info("contraseña cambiada usuario={}", StringUtils.abbreviate(usuario, 64));
    }

    private static void validarNueva(String usuario, String actual, String nueva) {
        if (StringUtils.length(nueva) < LARGO_MINIMO) {
            throw new IllegalArgumentException("La contraseña nueva debe tener al menos 8 caracteres.");
        }
        if (nueva.chars().noneMatch(Character::isLetter) || nueva.chars().noneMatch(Character::isDigit)) {
            throw new IllegalArgumentException("La contraseña nueva debe combinar letras y números.");
        }
        if (StringUtils.containsIgnoreCase(nueva, usuario)) {
            throw new IllegalArgumentException("La contraseña nueva no puede contener el nombre de usuario.");
        }
        if (nueva.equals(actual)) {
            throw new IllegalArgumentException("La contraseña nueva debe ser distinta de la actual.");
        }
    }
}
