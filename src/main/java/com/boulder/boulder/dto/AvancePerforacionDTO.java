package com.boulder.boulder.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

/**
 * Objeto de transferencia de datos para un parte diario de avance de perforación.
 *
 * <p>Incluye la dotación de máquina como lista anidada de {@link AvancePersonalDTO}.</p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Data
public class AvancePerforacionDTO {
    private UUID id;
    private Integer numeroInterno;

    private UUID sondajeId;
    private String sondajeCodigo;

    @NotNull(message = "La fecha del parte es obligatoria")
    private LocalDate fecha;

    private String turno;
    private String responsable;

    private UUID maquinaId;
    private String maquinaCodigo;
    private UUID plataformaId;
    private String plataformaCodigo;

    @NotNull(message = "La profundidad 'desde' es obligatoria")
    private BigDecimal desde;

    @NotNull(message = "La profundidad 'hasta' es obligatoria")
    private BigDecimal hasta;

    @NotNull(message = "El avance del turno es obligatorio")
    @PositiveOrZero(message = "El avance no puede ser negativo")
    private BigDecimal avance;

    private String casingTipo;
    private BigDecimal casingDesde;
    private BigDecimal casingHasta;
    private BigDecimal retornoPct;
    private String linea;
    private String comentarios;
    private String estado;
    private String observaciones;

    @Valid
    private List<AvancePersonalDTO> personal = new ArrayList<>();
}
