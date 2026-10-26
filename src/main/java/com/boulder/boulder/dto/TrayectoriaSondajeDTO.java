package com.boulder.boulder.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.Data;

/**
 * Trayectoria de un sondaje: lo planificado contra lo medido.
 *
 * <p>
 * Es lo que hoy se arma a mano en una planilla y se comparte como imagen por
 * WhatsApp: dos curvas —azimut e inclinación— a lo largo de la profundidad,
 * contra la línea recta del setup planificado. La separación entre ambas es el
 * desvío.
 * </p>
 *
 * <p>
 * El plan se expone como dos escalares y no como una serie porque eso es lo
 * que hay: el sondaje se planifica con un azimut y una inclinación de partida,
 * y el trazo planificado es esa recta. Convertirlo en serie del lado del
 * servidor sería repetir el mismo número una vez por estación.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Data
public class TrayectoriaSondajeDTO {

    private UUID sondajeId;
    private String sondajeCodigo;
    private String sondajeNombre;

    private Double azimutPlan;
    private Double inclinacionPlan;

    private Double profundidadActual;
    private Double profundidadFinal;

    /** Estaciones medidas, ordenadas por profundidad. */
    private List<EstacionSurveyDTO> estaciones = new ArrayList<>();

    /** Desviación absoluta de azimut en la estación más profunda. */
    private Double desvAzimutUltimo;

    /** Desviación absoluta de inclinación en la estación más profunda. */
    private Double desvInclinacionUltimo;

    /** Profundidad de la estación más profunda con medición. */
    private Double profundidadUltima;
}
