package com.boulder.boulder.config;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import jakarta.persistence.EntityNotFoundException;

/**
 * Manejador global de excepciones para la capa REST.
 *
 * <p>
 * Traduce las excepciones de negocio y de validación a respuestas HTTP
 * consistentes ({@code 404}, {@code 400}, {@code 409}, {@code 500}) con un
 * cuerpo homogéneo, evitando que se propaguen errores {@code 500} genéricos.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private Map<String, Object> body(HttpStatus status, String message, WebRequest request) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("timestamp", OffsetDateTime.now().toString());
        map.put("status", status.value());
        map.put("error", status.getReasonPhrase());
        map.put("message", message);
        if (request instanceof ServletWebRequest servletWebRequest) {
            map.put("path", servletWebRequest.getRequest().getRequestURI());
        }
        return map;
    }

    /** Entidad no encontrada -> 404. */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(EntityNotFoundException ex, WebRequest request) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(body(HttpStatus.NOT_FOUND, ex.getMessage(), request));
    }

    /** Argumentos inválidos de negocio -> 400. */
    @ExceptionHandler({ IllegalArgumentException.class, IllegalStateException.class })
    public ResponseEntity<Map<String, Object>> handleBadRequest(RuntimeException ex, WebRequest request) {
        log.warn("Solicitud inválida: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(body(HttpStatus.BAD_REQUEST, ex.getMessage(), request));
    }

    /** Fallo de validación de {@code @Valid} sobre el cuerpo -> 400. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        String detalle = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("Validación fallida: {}", detalle);
        Map<String, Object> map = body(HttpStatus.BAD_REQUEST, "Datos de entrada inválidos", request);
        map.put("errores", detalle);
        return ResponseEntity.badRequest().body(map);
    }

    /** Violación de integridad (FK, unique, check) -> 409. */
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleIntegrity(
            org.springframework.dao.DataIntegrityViolationException ex, WebRequest request) {
        String causa = ex.getMostSpecificCause().getMessage();
        log.warn("Violación de integridad de datos: {}", causa);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(body(HttpStatus.CONFLICT, causa, request));
    }

    /**
     * Falta de permisos -> 403.
     *
     * <p>
     * Debe declararse aparte: el resolutor de excepciones de MVC corre
     * <em>antes</em> que el {@code ExceptionTranslationFilter} de Spring
     * Security, así que sin este método el catch-all de abajo se tragaría el
     * rechazo de {@code @PreAuthorize} y lo devolvería como 500. Relanzarla
     * deja que la cadena de filtros la traduzca al 403 que corresponde.
     * </p>
     */
    @ExceptionHandler(AccessDeniedException.class)
    public void handleAccessDenied(AccessDeniedException ex) throws AccessDeniedException {
        throw ex;
    }

    /**
     * Cualquier otro error no controlado -> 500.
     *
     * <p>
     * Las excepciones propias de Spring MVC ({@code NoResourceFoundException}
     * para una ruta inexistente, {@code ResponseStatusException}, etc.)
     * implementan {@link ErrorResponse} y ya traen su propio código. Sin este
     * reenvío, pedir una URL que no existe se reportaba como "Error interno del
     * servidor", que apunta al sitio equivocado al diagnosticar.
     * </p>
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex, WebRequest request) {
        if (ex instanceof ErrorResponse errorResponse) {
            HttpStatus status = HttpStatus.valueOf(errorResponse.getStatusCode().value());
            log.warn("Petición rechazada por MVC ({}): {}", status.value(), ex.getMessage());
            return ResponseEntity.status(status)
                    .body(body(status, ex.getMessage(), request));
        }
        log.error("Error no controlado en la capa REST", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", request));
    }
}
