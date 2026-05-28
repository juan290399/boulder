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
@Table(name = "prf_maquina", schema = "operacional")
@Data
public class Maquina implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "numero_interno", insertable = false, updatable = false)
    private Integer numeroInterno;

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 50)
    private String tipo;

    @Column(name = "alcance_pq")
    private Short alcancePq;

    @Column(name = "alcance_hq")
    private Short alcanceHq;

    @Column(name = "alcance_nq")
    private Short alcanceNq;

    @Column(length = 50)
    private String marca;

    @Column(length = 50)
    private String motor;

    @Column(name = "fecha_compra")
    private LocalDate fechaCompra;

    @Column(name = "fecha_llegada")
    private LocalDate fechaLlegada;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(length = 30)
    private String estado = "DISPONIBLE";

    @Column(columnDefinition = "TEXT")
    private String notes;
}