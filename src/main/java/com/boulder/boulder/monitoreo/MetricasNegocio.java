package com.boulder.boulder.monitoreo;

import java.util.Locale;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.boulder.boulder.eventos.CambioDatos;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * Métricas propias del negocio, publicadas con Micrometer junto a las de la JVM
 * y del servidor HTTP.
 *
 * <p>
 * Las de la JVM dicen si el proceso está sano; estas dicen si el sistema hace
 * su trabajo: cuántas filas entran por cada canal (tablet, web, importación),
 * cuántas rechaza la base al sincronizar GeoLog y cómo van los inicios de
 * sesión. Los nombres viven aquí para que el plan de monitoreo y las alertas de
 * Prometheus tengan una sola referencia.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Component
public class MetricasNegocio {

    /** Filas confirmadas, con etiquetas {@code recurso} y {@code origen}. */
    public static final String FILAS = "boulder.datos.filas";
    /** Inicios de sesión, con etiqueta {@code resultado}. */
    public static final String LOGIN = "boulder.login";
    /** Filas de logueo rechazadas al sincronizar, con etiqueta {@code plantilla}. */
    public static final String RECHAZOS = "boulder.logueo.rechazadas";

    /** Resultado de un intento de inicio de sesión. */
    public enum ResultadoLogin { CORRECTO, FALLIDO, BLOQUEADO }

    private final MeterRegistry registro;

    public MetricasNegocio(MeterRegistry registro) {
        this.registro = registro;
    }

    /**
     * Suma las filas de cada cambio confirmado. Escucha el mismo aviso que
     * alimenta el canal de eventos del navegador, así que no hace falta tocar
     * cada servicio que guarda datos.
     *
     * @param cambio aviso publicado tras el commit
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void alCambiar(CambioDatos cambio) {
        Counter.builder(FILAS)
                .description("Filas confirmadas por recurso y canal de entrada")
                .tag("recurso", cambio.recurso())
                .tag("origen", cambio.origen())
                .register(registro)
                .increment(cambio.filas());
    }

    /**
     * Cuenta un intento de inicio de sesión.
     *
     * @param resultado cómo terminó el intento
     */
    public void login(ResultadoLogin resultado) {
        Counter.builder(LOGIN)
                .description("Intentos de inicio de sesión por resultado")
                .tag("resultado", resultado.name().toLowerCase(Locale.ROOT))
                .register(registro)
                .increment();
    }

    /**
     * Cuenta las filas de un lote de GeoLog que la base rechazó.
     *
     * @param plantilla plantilla del lote (quicklog, litologia, ...)
     * @param filas     filas rechazadas; cero no registra nada
     */
    public void rechazosLogueo(String plantilla, int filas) {
        if (filas <= 0) {
            return;
        }
        Counter.builder(RECHAZOS)
                .description("Filas de logueo rechazadas al sincronizar")
                .tag("plantilla", plantilla)
                .register(registro)
                .increment(filas);
    }
}
