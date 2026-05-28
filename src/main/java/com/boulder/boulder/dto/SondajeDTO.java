package com.boulder.boulder.dto;

import java.time.LocalDate;
import java.util.UUID;

import lombok.Data;

@Data
public class SondajeDTO {
    private UUID id;
    private String codigo;
    private String nombre;
    
    // Datos aplanados de las relaciones
    private UUID proyectoId;
    private UUID programaSondajeId;
    private String programaSondajeNombre;
    private UUID collarId;
    private String collarCodigo;
    private UUID plataformaId;
    private String plataformaCodigo;
    private UUID maquinaId;
    private String maquinaNombre;
    
    // Geometría y avances
    private Double azimutSetup;
    private Double inclinacionSetup;
    private Double profundidadActual;
    private Double profundidadFinal;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
}