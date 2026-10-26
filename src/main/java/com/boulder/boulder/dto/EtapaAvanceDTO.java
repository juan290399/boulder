package com.boulder.boulder.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Una etapa del flujo de testigo dentro del frente de avance de un sondaje.
 *
 * <p>
 * El rezago se calcula aquí y no en el navegador a propósito. Es una regla de
 * negocio —qué tan atrás va una etapa respecto de lo perforado— y si vive en
 * TypeScript hay que reescribirla en cada pantalla que la necesite, además de
 * quedar fuera del alcance de cualquier prueba del backend.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EtapaAvanceDTO {

    /** Identificador estable para la interfaz: perforacion, logueo, muestreo... */
    private String clave;

    /** Etiqueta legible de la etapa. */
    private String nombre;

    /** Profundidad alcanzada por la etapa, o {@code null} si nunca se registró. */
    private BigDecimal profundidad;

    /** Metros que la etapa lleva de atraso respecto de lo perforado. */
    private BigDecimal rezago;

    /** Porcentaje de lo perforado que la etapa ya cubrió. */
    private BigDecimal pctSobrePerforado;
}
