package com.boulder.boulder.entities;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "prf_plataforma", schema = "operacional")
@Data
public class Plataforma implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "numero_interno", insertable = false, updatable = false)
    private Integer numeroInterno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id", referencedColumnName = "id", nullable = false)
    private Proyecto proyecto;

    @Column(nullable = false, length = 50)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false)
    private Double este;

    @Column(nullable = false)
    private Double norte;

    @Column(nullable = false)
    private Double elevacion;

    @Column(name = "eje_mayor")
    private Double ejeMayor;

    @Column(name = "eje_minor")
    private Double ejeMinor;

    @Column(name = "eje_z")
    private Double ejeZ;

    private Double area;

    @Column(name = "numero_pozas")
    private Short numeroPozas = 0;

    @Column(length = 30)
    private String estado;

    @Column(length = 50)
    private String tipo;

    @Column(columnDefinition = "TEXT")
    private String ubicacion;

    @Column(columnDefinition = "TEXT")
    private String notas;
}