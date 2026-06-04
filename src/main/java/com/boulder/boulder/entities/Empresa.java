package com.boulder.boulder.entities;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Representa una empresa registrada en el sistema.
 *
 * <p>Almacena la información básica de identificación y contacto utilizada
 * para gestionar clientes, contratistas, proveedores u otras organizaciones
 * relacionadas con los proyectos.</p>
 *
 * <p>Se persiste en la tabla {@code base.empresa}.</p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "empresa", schema = "base")
@Data
public class Empresa implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "numero_interno", insertable = false, updatable = false)
    private Integer numeroInterno;

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 20)
    private String ruc;

    @Column(columnDefinition = "TEXT")
    private String direccion;

    @Column(length = 50)
    private String telefono;

    @Column(length = 100)
    private String correo;
}