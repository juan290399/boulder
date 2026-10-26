package com.boulder.boulder.config;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.time.Duration;
import java.util.Locale;

import com.google.common.base.Ticker;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Limita los intentos fallidos de inicio de sesión por usuario, contra ataques
 * de fuerza bruta sobre las contraseñas (OWASP A07: fallas de identificación y
 * autenticación).
 *
 * <p>Los fallos se cuentan en una caché de Guava cuyas entradas vencen un tiempo
 * después de su última escritura: cada fallo nuevo reinicia el plazo, así que el
 * bloqueo dura {@code bloqueo} desde el último intento. Un acceso correcto borra
 * el contador. La caché tiene tamaño máximo, para que un atacante que pruebe
 * miles de usuarios inventados no agote la memoria.</p>
 *
 * <p>El estado vive en memoria: si se reinicia el servidor, los contadores se
 * pierden. Para un solo servidor de campamento es suficiente.</p>
 */
public class LimiteIntentosLogin {

    private static final long MAX_USUARIOS_SEGUIDOS = 10_000;

    private final int maxIntentos;
    private final Cache<String, Integer> fallos;

    /**
     * @param maxIntentos fallos seguidos que bloquean la cuenta; al menos 1
     * @param bloqueo     cuánto dura el bloqueo desde el último fallo; positivo
     * @param reloj       fuente de tiempo; {@link Ticker#systemTicker()} en producción
     */
    public LimiteIntentosLogin(int maxIntentos, Duration bloqueo, Ticker reloj) {
        checkArgument(maxIntentos >= 1, "maxIntentos debe ser al menos 1: %s", maxIntentos);
        checkNotNull(bloqueo, "bloqueo");
        checkArgument(!bloqueo.isNegative() && !bloqueo.isZero(), "el bloqueo debe ser positivo: %s", bloqueo);
        this.maxIntentos = maxIntentos;
        this.fallos = CacheBuilder.newBuilder()
                .expireAfterWrite(bloqueo)
                .maximumSize(MAX_USUARIOS_SEGUIDOS)
                .ticker(checkNotNull(reloj, "reloj"))
                .build();
    }

    /**
     * @param usuario nombre de usuario tal como lo escribió la persona
     * @return {@code true} si la cuenta alcanzó el máximo de fallos y el plazo no venció
     */
    public boolean bloqueado(String usuario) {
        Integer n = fallos.getIfPresent(clave(usuario));
        return n != null && n >= maxIntentos;
    }

    /** Suma un fallo y reinicia el plazo de bloqueo. */
    public void registrarFallo(String usuario) {
        fallos.asMap().merge(clave(usuario), 1, Integer::sum);
    }

    /** Un acceso correcto deja la cuenta sin fallos. */
    public void registrarExito(String usuario) {
        fallos.invalidate(clave(usuario));
    }

    private static String clave(String usuario) {
        return usuario == null ? "" : usuario.strip().toLowerCase(Locale.ROOT);
    }
}
