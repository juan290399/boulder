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

/**
 * Representa un proyecto de exploración geológica.
 *
 * <p>
 * Contiene la información base del proyecto, su ubicación, sistema de
 * referencia espacial y la empresa responsable de su ejecución.
 * </p>
 *
 * <p>
 * Se almacena en la tabla {@code base.proyecto}.
 * </p>
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "proyecto", schema = "base")
@Data
public class Proyecto implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "numero_interno", insertable = false, updatable = false)
    private Integer numeroInterno;

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String ubicacion;

    private Integer srid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empresa", referencedColumnName = "id", nullable = false)
    private Empresa empresa;
}