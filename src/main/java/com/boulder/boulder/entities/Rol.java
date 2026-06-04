package com.boulder.boulder.entities;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa un rol de usuario dentro del sistema.
 *
 * <p>
 * Define los perfiles de acceso utilizados para control de permisos
 * y autorización dentro de la plataforma.
 * </p>
 *
 * <p>
 * Se almacena en la tabla {@code base.rol}.
 * </p>
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "rol", schema = "base")
public class Rol implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "numero_interno", insertable = false, updatable = false)
    private Integer numeroInterno;

    @Column(nullable = false, length = 255)
    private String codigo;

    @Column(nullable = false, length = 255)
    private String nombre;
}
