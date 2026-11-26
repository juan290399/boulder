package com.boulder.boulder.reportes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.ImmutableList;

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

    /**
     * @param cabecera datos del sondaje
     * @param tramos   tramos en cualquier orden
     * @return la hoja con los tramos ordenados por profundidad
     */
    public static HojaQuicklog de(Cabecera cabecera, List<Tramo> tramos) {
        return new HojaQuicklog(cabecera, tramos.stream()
                .sorted(Comparator.comparing(Tramo::desde))
                .collect(ImmutableList.toImmutableList()));
    }

    /** Geólogos que loguearon el sondaje, en el orden en que aparecen. */
    public List<String> geologos() {
        return tramos.stream().map(Tramo::geologo).filter(Objects::nonNull).distinct().toList();
    }

    public BigDecimal metrosLogueados() {
        return tramos.stream().map(Tramo::longitud).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
