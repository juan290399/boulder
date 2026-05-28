package com.boulder.boulder.entities;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "prf_collar", schema = "operacional")
@Data
public class Collar implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "numero_interno", insertable = false, updatable = false)
    private Integer numeroInterno;

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false)
    private Double este;

    @Column(nullable = false)
    private Double norte;

    @Column(nullable = false)
    private Double elevacion;

    @Column(name = "fecha_medicion")
    private LocalDate fechaMedicion;

    @Column(length = 50)
    private String campania;

    @Column(columnDefinition = "TEXT")
    private String notas;
}