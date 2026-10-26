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
        // Diferencia de azimut llevada a (−180, 180]: de 358° a 2° son 4° a la
        // derecha, no 356° a la izquierda.
        double azimut = ((azimutMedido - azimutPlan) % 360 + 540) % 360 - 180;
        double inclinacion = inclinacionMedida - inclinacionPlan;

        double[] medida = direccion(azimutMedido, inclinacionMedida);
        double[] plan = direccion(azimutPlan, inclinacionPlan);
        // atan2(|a×b|, a·b) y no acos(a·b): cerca de 0°, que es justo el caso
        // normal, acos pierde la mitad de los decimales.
        double punto = medida[0] * plan[0] + medida[1] * plan[1] + medida[2] * plan[2];
        double cx = medida[1] * plan[2] - medida[2] * plan[1];
        double cy = medida[2] * plan[0] - medida[0] * plan[2];
        double cz = medida[0] * plan[1] - medida[1] * plan[0];
        double total = Math.toDegrees(Math.atan2(Math.sqrt(cx * cx + cy * cy + cz * cz), punto));

        return new Desviacion(azimut, sentido(azimut, "derecha", "izquierda"),
                inclinacion, sentido(inclinacion, "arriba", "abajo"),
                total, profundidad * Math.sin(Math.toRadians(total)));
    }

    /** Vector unitario (este, norte, arriba) de una dirección de pozo. */
    private static double[] direccion(double azimut, double inclinacion) {
        double az = Math.toRadians(azimut);
        double inc = Math.toRadians(inclinacion);
        return new double[] { Math.cos(inc) * Math.sin(az), Math.cos(inc) * Math.cos(az), Math.sin(inc) };
    }

    private static String sentido(double diferencia, String positivo, String negativo) {
        if (Math.abs(diferencia) < 1e-9) {
            return null;
        }
        return diferencia > 0 ? positivo : negativo;
    }
}
