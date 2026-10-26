package com.boulder.boulder.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRolDTO {
    private UUID id;   
    private UUID usuarioId;
    private String usuarioName;
    private UUID rolId;   
    private String rolNombre;
}