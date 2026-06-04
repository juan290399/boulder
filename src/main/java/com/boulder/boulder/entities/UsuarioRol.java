package com.boulder.boulder.entities;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
 * Representa la asignación de un rol a un usuario dentro del sistema.
 *
 * <p>
 * Es una entidad intermedia (tabla de relación muchos a muchos)
 * entre Usuario y Rol, con restricción de unicidad para evitar
 * duplicados de asignación.
 * </p>
 *
 * <p>
 * Se persiste en la tabla {@code base.usuario_rol}.
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
@Table(name = "usuario_rol", schema = "base", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"id_usuario", "id_rol"})
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class UsuarioRol implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    @JsonBackReference
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;
}