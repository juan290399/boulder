package com.boulder.boulder.reportes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Hoja de registro de quicklog de un sondaje: cabecera del sondaje y un renglón por tramo.
 *
 * @param cabecera datos del sondaje
 * @param tramos   tramos por profundidad
 */
public record HojaQuicklog(Cabecera cabecera, List<Tramo> tramos) {

    /** Datos del sondaje que encabezan la hoja. */
    public record Cabecera(String sondaje, String ubicacion, String maquina, Double azimut, Double dip,
            Double norte, Double este, Double cota, Double profundidadProgramada) {
    }

    /** Un tramo logueado; los códigos múltiples van unidos con guion. */
    public record Tramo(LocalDate fecha, BigDecimal desde, BigDecimal hasta, BigDecimal longitud, String lito,
            String alteracion, String ensambleAlteracion, String mineralizacion, BigDecimal pcPy, BigDecimal pcCu,
            BigDecimal pcMo, String estilo, String zona, String comentario, String geologo) {
    }

    public static HojaQuicklog de(Cabecera cabecera, List<Tramo> tramos) {
        return new HojaQuicklog(cabecera, tramos);
    }

    public List<String> geologos() {
        return List.of();
    }

    public BigDecimal metrosLogueados() {
        return BigDecimal.ZERO;
    }
}
