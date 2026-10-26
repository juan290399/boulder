package com.boulder.boulder.dominio;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Acumula los motivos de rechazo de un parte de avance.
 *
 * <p>Los siete partes de testigo comparten las mismas comprobaciones (un tramo
 * que no termina antes de empezar, un valor dentro de su lista, un avance que
 * cuadra); cada uno elige cuáles le tocan. Devolver todos los motivos juntos, y
 * no solo el primero, ahorra al usuario corregir y reenviar una vez por error.</p>
 */
public final class Revision {

    private final List<String> errores = new ArrayList<>();

    /** Un tramo en metros: si vienen los dos extremos, hasta no puede ser menor que desde. */
    public Revision tramo(String nombre, BigDecimal desde, BigDecimal hasta) {
        return this;
    }

    /** Un rango de cajas: si vienen los dos extremos, la última no puede ser anterior a la primera. */
    public Revision cajas(String nombre, Integer desde, Integer hasta) {
        return this;
    }

    /** Si vienen los tres números, el avance tiene que ser hasta menos desde. */
    public Revision avance(BigDecimal desde, BigDecimal hasta, BigDecimal avance) {
        return this;
    }

    /** Un valor de una lista cerrada; {@code null} se acepta (el campo es opcional). */
    public Revision opcion(String campo, String valor, String... permitidos) {
        return this;
    }

    /** Un porcentaje entre 0 y 100; {@code null} se acepta. */
    public Revision porcentaje(String campo, BigDecimal valor) {
        return this;
    }

    public List<String> errores() {
        return List.copyOf(errores);
    }

    public boolean valida() {
        return errores.isEmpty();
    }
}
