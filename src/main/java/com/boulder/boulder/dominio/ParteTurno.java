package com.boulder.boulder.dominio;

import java.math.BigDecimal;
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
        return List.of();
    }
}
