package com.boulder.boulder.dto.importacion;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * Informe de una importación, hoja por hoja.
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Data
public class ResultadoImportacionDTO {

    /** Clave de la plantilla procesada. */
    private String plantilla;

    /** Nombre del archivo que subió el usuario. */
    private String archivo;

    /**
     * {@code false} cuando la corrida fue una simulación: se leyó, se validó y
     * no se escribió nada.
     */
    private boolean aplicado;

    private LocalDateTime momento = LocalDateTime.now();

    private int totalLeidas;
    private int totalAceptadas;
    private int totalOmitidas;
    private int totalRechazadas;

    private List<ResultadoHojaDTO> hojas = new ArrayList<>();

    /** Recalcula los totales a partir de las hojas ya procesadas. */
    public void totalizar() {
        totalLeidas = hojas.stream().mapToInt(ResultadoHojaDTO::getLeidas).sum();
        totalAceptadas = hojas.stream().mapToInt(ResultadoHojaDTO::getAceptadas).sum();
        totalOmitidas = hojas.stream().mapToInt(ResultadoHojaDTO::getOmitidas).sum();
        totalRechazadas = hojas.stream().mapToInt(ResultadoHojaDTO::getRechazadas).sum();
    }
}
