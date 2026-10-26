package com.boulder.boulder.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.Data;

/**
 * Estado consolidado de un sondaje a lo largo del flujo de testigo.
 *
 * <p>
 * Es la unidad de la pantalla del geólogo: un sondaje, la profundidad que
 * alcanzó cada una de las siete etapas, y cuál de ellas es la que más atrás
 * va. Reemplaza la lectura cruzada de siete listados sueltos.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Data
public class FrenteAvanceDTO {

    private UUID sondajeId;
    private String sondajeCodigo;
    private String sondajeNombre;

    private BigDecimal profundidadActual;
    private BigDecimal profundidadFinal;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    /** Fecha del parte más reciente de cualquiera de las siete etapas. */
    private LocalDate ultimaActividad;

    /** Un sondaje sin fecha de cierre sigue en curso. */
    private boolean activo;

    /** Días transcurridos desde el último parte; {@code null} si nunca hubo uno. */
    private Long diasSinMovimiento;

    private Long muestrasEmbolsadas;
    private Long cajasRecibidas;

    /** Avance de la perforación sobre la profundidad programada, en porcentaje. */
    private BigDecimal pctProgramado;

    private List<EtapaAvanceDTO> etapas = new ArrayList<>();

    /** Clave de la etapa con mayor rezago; es de lo que hay que ocuparse primero. */
    private String etapaCritica;

    /** Metros de rezago de la etapa crítica. */
    private BigDecimal rezagoMaximo;
}
