package com.boulder.boulder.entities;

import java.io.Serializable;
import java.time.LocalDate;
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
 * Persona que trabaja en la operación, con independencia de que tenga cuenta
 * de acceso.
 *
 * <p>
 * Sustituye a la antigua {@code operacional.prf_perforista}, que solo cubría
 * la dotación de perforación y dejaba fuera a geólogos, ayudantes de geología
 * y personal de laboratorio: esos solo existían como texto escrito a mano en
 * las columnas {@code responsable} y {@code geologo} de cada parte.
 * </p>
 *
 * <p>
 * La migración conservó los identificadores de los perforistas, así que las
 * referencias desde {@link AvancePersonal} y {@link SurveyMedicion} siguen
 * siendo válidas.
 * </p>
 *
 * <p>Se persiste en la tabla {@code base.empleado}.</p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "empleado", schema = "base")
@Data
public class Empleado implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "numero_interno", insertable = false, updatable = false)
    private Integer numeroInterno;

    @Column(nullable = false, unique = true, length = 15)
    private String codigo;

    @Column(nullable = false, length = 80)
    private String nombres;

    @Column(length = 80)
    private String apellidos;

    @Column(length = 20)
    private String documento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cargo_id")
    private Cargo cargo;

    @Column(length = 120)
    private String email;

    @Column(length = 30)
    private String telefono;

    @Column(name = "fecha_ingreso")
    private LocalDate fechaIngreso;

    @Column(name = "fecha_salida")
    private LocalDate fechaSalida;

    @Column(nullable = false)
    private Boolean estado = Boolean.TRUE;

    @Column(columnDefinition = "TEXT")
    private String notas;
}
