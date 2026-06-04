package com.boulder.boulder.entities;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Relación entre proyectos y máquinas de perforación.
 *
 * <p>
 * Permite controlar la asignación de máquinas a un proyecto,
 * registrando fechas de ingreso y salida, así como su estado operativo
 * dentro del proyecto.
 * </p>
 *
 * <p>
 * Incluye una restricción de unicidad por proyecto, máquina y fecha de ingreso.
 * </p>
 *
 * <p>
 * Se almacena en la tabla {@code operacional.prf_proyecto_maquina}.
 * </p>
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "prf_proyecto_maquina", schema = "operacional", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"proyecto_id", "maquina_id", "fecha_ingreso"})
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ProyectoMaquina implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "numero_interno", insertable = false, updatable = false)
    private Integer numeroInterno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maquina_id", nullable = false)
    private Maquina maquina;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso;

    @Column(name = "fecha_salida")
    private LocalDate fechaSalida;

    @Column(length = 30)
    private String estado;
}