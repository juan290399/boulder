package com.boulder.boulder.monitoreo;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Da a cada petición un identificador que acompaña a todas sus líneas de
 * bitácora (clave {@code peticion} del MDC) y vuelve al cliente en la cabecera
 * {@code X-Request-Id}.
 *
 * <p>
 * Con él, un error que reporta un usuario se encuentra en el log por su id, y
 * las líneas de una misma petición se pueden filtrar en el log JSON. Si el
 * proxy ya trae un id válido se respeta, para seguir la petición de Nginx a la
 * aplicación; uno con caracteres raros se descarta, porque terminaría escrito
 * en la bitácora (CWE-117).
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class FiltroIdPeticion extends OncePerRequestFilter {

    /** Cabecera de entrada y de salida. */
    public static final String CABECERA = "X-Request-Id";
    /** Clave del MDC que usan los patrones de Logback. */
    public static final String CLAVE_MDC = "peticion";

    private static final Pattern VALIDO = Pattern.compile("[A-Za-z0-9._-]{1,64}");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String entrante = request.getHeader(CABECERA);
        String id = entrante != null && VALIDO.matcher(entrante).matches()
                ? entrante
                : UUID.randomUUID().toString().substring(0, 13);
        MDC.put(CLAVE_MDC, id);
        response.setHeader(CABECERA, id);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(CLAVE_MDC);
        }
    }
}
