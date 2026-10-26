package com.boulder.boulder.dominio;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Reglas aritméticas del parte de perforación por turno.
 *
 * <p>El perforista cierra el turno con tres números: desde, hasta y avance.
 * El avance es la resta de los otros dos, así que si no cuadra, uno de los tres
 * está mal tecleado y no hay forma de saber cuál. Las mismas reglas valen para
 * el formulario y para la carga desde Excel.</p>
 */
public final class ParteTurno {

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    private ParteTurno() {
    }

    /**
     * Revisa los números de un parte de turno.
     *
     * @param desde       profundidad al inicio del turno, en metros
     * @param hasta       profundidad al cierre del turno, en metros
     * @param avance      metros perforados en el turno
     * @param retornoPct  porcentaje de retorno de agua; puede ser {@code null}
     * @param casingDesde inicio del tramo entubado; puede ser {@code null}
     * @param casingHasta fin del tramo entubado; puede ser {@code null}
     * @return los motivos de rechazo, vacío si el parte cuadra
     */
    public static List<String> errores(BigDecimal desde, BigDecimal hasta, BigDecimal avance,
            BigDecimal retornoPct, BigDecimal casingDesde, BigDecimal casingHasta) {
        List<String> errores = new ArrayList<>();
        if (desde != null && desde.signum() < 0) {
            errores.add("desde (" + desde.toPlainString() + ") no puede ser negativo");
        }
        if (desde != null && hasta != null && hasta.compareTo(desde) < 0) {
            errores.add("hasta (" + hasta.toPlainString() + ") no puede ser menor que desde ("
                    + desde.toPlainString() + ")");
        } else if (desde != null && hasta != null && avance != null
                && hasta.subtract(desde).compareTo(avance) != 0) {
            errores.add("avance (" + avance.toPlainString() + ") no coincide con hasta menos desde ("
                    + hasta.subtract(desde).toPlainString() + ")");
        }
        if (retornoPct != null && (retornoPct.signum() < 0 || retornoPct.compareTo(CIEN) > 0)) {
            errores.add("retorno_pct: tiene que estar entre 0 y 100");
        }
        if (casingDesde != null && casingHasta != null && casingHasta.compareTo(casingDesde) < 0) {
            errores.add("casing: termina (" + casingHasta.toPlainString() + ") antes de empezar ("
                    + casingDesde.toPlainString() + ")");
        }
        if (casingHasta != null && hasta != null && casingHasta.compareTo(hasta) > 0) {
            errores.add("casing: baja hasta " + casingHasta.toPlainString() + " m y el pozo llega a "
                    + hasta.toPlainString() + " m");
        }
        return errores;
    }
}
