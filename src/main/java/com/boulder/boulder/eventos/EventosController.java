package com.boulder.boulder.eventos;

import java.security.Principal;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.RequiredArgsConstructor;

/**
 * Canal de avisos en tiempo real.
 *
 * <p>
 * Pide token como cualquier otra ruta. El {@code EventSource} del navegador no
 * sabe mandar la cabecera {@code Authorization}, así que el frontend abre este
 * flujo con {@code fetch}, que sí puede: se evita pasar el token por la URL,
 * donde queda escrito en los registros de cualquier proxy.
 * </p>
 */
@RestController
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
public class EventosController {

    private final CanalEventos canal;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter suscribir(Principal principal) {
        return canal.suscribir(principal != null ? principal.getName() : "anonimo");
    }
}
