package com.boulder.boulder.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

/**
 * El 409 por violación de integridad no expone nombres de tablas, columnas ni
 * restricciones de PostgreSQL.
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private ResponseEntity<Map<String, Object>> responder(String sqlState, String detalle) {
        SQLException causa = new SQLException(detalle, sqlState);
        DataIntegrityViolationException ex = new DataIntegrityViolationException("could not execute statement", causa);
        return handler.handleIntegrity(ex, new ServletWebRequest(new MockHttpServletRequest("POST", "/api/x")));
    }

    @ParameterizedTest(name = "SQLState {0} -> {2}")
    @CsvSource(delimiter = '|', value = {
        "23505|duplicate key value violates unique constraint \"uq_quicklog_tramo\"|Ya existe un registro con esos datos",
        "23503|insert or update on table \"ope_parte\" violates foreign key constraint \"fk_parte_sondaje\"|Hace referencia a un registro que no existe o que está en uso",
        "23514|new row for relation \"ope_parte\" violates check constraint \"ck_hasta_mayor\"|Un valor no cumple las reglas del registro",
        "23502|null value in column \"codigo\" violates not-null constraint|Falta un dato obligatorio",
        "23P01|conflicting key value violates exclusion constraint \"ex_tramo\"|Ya existe un registro con esos datos",
        "XX000|algo raro en \"tabla_interna\"|Los datos no cumplen las reglas de integridad"
    })
    @DisplayName("El mensaje es propio y no nombra la restricción")
    void mensajePropio(String sqlState, String detalle, String esperado) {
        ResponseEntity<Map<String, Object>> r = responder(sqlState, detalle);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        String mensaje = (String) r.getBody().get("message");
        assertThat(mensaje).isEqualTo(esperado);
        assertThat(mensaje).doesNotContain("\"").doesNotContain("constraint");
    }
}
