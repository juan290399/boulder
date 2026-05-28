package com.boulder.boulder.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class MaquinaDTO {
    private UUID id;
    private String codigo;
    private String nombre;
    private String tipo;
    private String marca;
    private String estado;
}