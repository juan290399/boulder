package com.boulder.boulder.dominio;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableSet;

/**
 * Acumula los motivos de rechazo de un parte de avance.
 *
 * <p>Los siete partes de testigo comparten las mismas comprobaciones (un tramo
 * que no termina antes de empezar, un valor dentro de su lista, un avance que
 * cuadra); cada uno elige cuáles le tocan. Devolver todos los motivos juntos, y
 * no solo el primero, ahorra al usuario corregir y reenviar una vez por error.</p>
 */
public final class Revision {

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    private final List<String> errores = new ArrayList<>();

    /** Un tramo en metros: si vienen los dos extremos, hasta no puede ser menor que desde. */
    public Revision tramo(String nombre, BigDecimal desde, BigDecimal hasta) {
        if (desde != null && hasta != null && hasta.compareTo(desde) < 0) {
            errores.add(nombre + ": termina en " + hasta.toPlainString() + " m, antes de empezar ("
                    + desde.toPlainString() + " m)");
        }
        return this;
    }

    /** Un rango de cajas: si vienen los dos extremos, la última no puede ser anterior a la primera. */
    public Revision cajas(String nombre, Integer desde, Integer hasta) {
        if (desde != null && hasta != null && hasta < desde) {
            errores.add(nombre + ": la última caja (" + hasta + ") es anterior a la primera (" + desde + ")");
        }
        return this;
    }

    /** Si vienen los tres números, el avance tiene que ser hasta menos desde. */
    public Revision avance(BigDecimal desde, BigDecimal hasta, BigDecimal avance) {
        if (desde != null && hasta != null && avance != null && hasta.subtract(desde).compareTo(avance) != 0) {
            errores.add("avance (" + avance.toPlainString() + ") no coincide con hasta menos desde ("
                    + hasta.subtract(desde).toPlainString() + ")");
        }
        return this;
    }

    /** Un valor de una lista cerrada; {@code null} se acepta (el campo es opcional). */
    public Revision opcion(String campo, String valor, String... permitidos) {
        if (valor != null && !ImmutableSet.copyOf(permitidos).contains(valor)) {
            errores.add(campo + ": «" + valor + "» no es válido; se espera " + String.join(", ", permitidos));
        }
        return this;
    }

    /** Un porcentaje entre 0 y 100; {@code null} se acepta. */
    public Revision porcentaje(String campo, BigDecimal valor) {
        if (valor != null && (valor.signum() < 0 || valor.compareTo(CIEN) > 0)) {
            errores.add(campo + ": tiene que estar entre 0 y 100");
        }
        return this;
    }

    public List<String> errores() {
        return List.copyOf(errores);
    }

    public boolean valida() {
        return errores.isEmpty();
    }
}
