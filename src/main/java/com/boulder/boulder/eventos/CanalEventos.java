package com.boulder.boulder.eventos;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Reparte los avisos de cambio a los navegadores conectados (Server-Sent Events).
 *
 * <p>
 * <b>Por qué SSE y no WebSocket.</b> El aviso va en un solo sentido, del
 * servidor al navegador. SSE es HTTP normal, pasa por cualquier proxy y Spring
 * MVC lo trae con {@link SseEmitter}, sin broker STOMP ni WebFlux.
 * </p>
 *
 * <p>
 * <b>Por qué después del commit.</b> Si se avisara antes, la pantalla podría
 * pedir los datos cuando todavía no son visibles —o cuando la transacción
 * termina deshaciéndose— y quedarse con la versión vieja sin enterarse.
 * {@code fallbackExecution} cubre los cambios hechos fuera de una transacción.
 * </p>
 *
 * <p>
 * Los suscriptores viven en memoria. Alcanza para un solo servidor; si algún
 * día hay más de uno, el reparto tiene que pasar por la base
 * ({@code LISTEN/NOTIFY}) para que un cambio en uno llegue a los navegadores
 * conectados al otro.
 * </p>
 */
@Component
public class CanalEventos {

    private static final Logger log = LoggerFactory.getLogger(CanalEventos.class);

    /** Sin límite propio: se corta cuando el navegador se va o la red cae. */
    private static final long SIN_TIMEOUT = 0L;

    private final List<SseEmitter> suscriptores = new CopyOnWriteArrayList<>();

    public SseEmitter suscribir(String usuario) {
        SseEmitter emisor = new SseEmitter(SIN_TIMEOUT);
        suscriptores.add(emisor);
        emisor.onCompletion(() -> suscriptores.remove(emisor));
        emisor.onTimeout(() -> suscriptores.remove(emisor));
        emisor.onError(e -> suscriptores.remove(emisor));
        enviar(emisor, "conectado", "{\"usuario\":\"" + usuario + "\"}");
        log.info("SSE: {} conectado ({} en línea)", usuario, suscriptores.size());
        return emisor;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void alConfirmarse(CambioDatos cambio) {
        if (suscriptores.isEmpty()) {
            return;
        }
        log.debug("SSE: {} en {} sondaje(s), a {} suscriptor(es)",
                cambio.recurso(), cambio.sondajes().size(), suscriptores.size());
        for (SseEmitter emisor : suscriptores) {
            enviar(emisor, "cambio", cambio);
        }
    }

    /**
     * Latido cada 25 s. Sin tráfico, algunos proxies y el propio navegador dan
     * la conexión por muerta; además es como se detecta y limpia a quien se fue
     * sin avisar.
     */
    @Scheduled(fixedRate = 25_000)
    public void latido() {
        for (SseEmitter emisor : suscriptores) {
            try {
                emisor.send(SseEmitter.event().comment("latido"));
            } catch (IOException | IllegalStateException e) {
                suscriptores.remove(emisor);
            }
        }
    }

    public int conectados() {
        return suscriptores.size();
    }

    private void enviar(SseEmitter emisor, String nombre, Object datos) {
        try {
            emisor.send(SseEmitter.event().name(nombre).data(datos, MediaType.APPLICATION_JSON));
        } catch (IOException | IllegalStateException e) {
            // El navegador ya se fue; no es un error del servidor.
            suscriptores.remove(emisor);
        }
    }
}
