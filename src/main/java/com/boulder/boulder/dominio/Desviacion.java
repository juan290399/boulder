package com.boulder.boulder.dominio;

/**
 * Desviación de una estación de survey respecto de la trayectoria planificada.
 *
 * <p>Convenciones de la campaña: el azimut va de 0 a 360° en sentido horario
 * desde el norte, y la inclinación es negativa hacia abajo (−60° es un pozo que
 * baja a 60° de la horizontal). Por eso, una inclinación medida mayor que la
 * planificada (−58° contra −60°) significa que el pozo se está levantando.</p>
 *
 * @param azimut          diferencia de azimut, de −180 a 180°; positiva hacia la derecha
 * @param sentidoAzimut   {@code derecha}, {@code izquierda} o {@code null} si no hay diferencia
 * @param inclinacion     diferencia de inclinación en grados; positiva hacia arriba
 * @param sentidoInclinacion {@code arriba}, {@code abajo} o {@code null}
 * @param total           ángulo entre la dirección medida y la planificada, en grados
 * @param metros          separación lateral estimada a esa profundidad, en metros
 */
public record Desviacion(double azimut, String sentidoAzimut, double inclinacion, String sentidoInclinacion,
        double total, double metros) {

    /**
     * Calcula la desviación de una estación.
     *
     * <p>La separación en metros es una estimación: supone que el pozo sale recto
     * del collar con la dirección medida, y la compara con la planificada
     * (profundidad × seno del ángulo total). Sirve para decidir si corregir; la
     * trayectoria real la da el cálculo de curvatura mínima con todas las
     * estaciones.</p>
     */
    public static Desviacion calcular(double profundidad, double azimutMedido, double inclinacionMedida,
            double azimutPlan, double inclinacionPlan) {
        return new Desviacion(0, null, 0, null, 0, 0);
    }
}
