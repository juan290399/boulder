package com.boulder.boulder.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa un usuario del sistema.
 *
 * <p>
 * Contiene las credenciales de acceso y la relación con los roles asignados,
 * permitiendo la gestión de autenticación y autorización dentro del sistema.
 * </p>
 *
 * <p>
 * Se persiste en la tabla {@code base.usuario}.
 * </p>
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Entity
@JsonIgnoreProperties({"usuarioRol"})
@Table(name = "\"usuario\"", schema = "base")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "numero_interno", insertable = false, updatable = false)
    private Integer numeroInterno;

    @Column(name = "nombres", nullable = false, length = 100)
    private String nombres;

    @Column(name = "apellidos", nullable = false, length = 100)
    private String apellidos;

    @Column(name = "correo", nullable = false, unique = true, length = 100)
    private String correo;

    @Column(name = "usuario", nullable = false, unique = true, length = 50)
    private String usuario;

    @Column(name = "contrasenia", nullable = false, length = 255)
    private String contrasenia;

    @Column(name = "estado", nullable = false)
    private Boolean estado = true;

    /**
     * Empleado al que pertenece la cuenta, si lo hay.
     *
     * <p>
     * Opcional en los dos sentidos: el auto-registro crea cuentas de gente que
     * todavía no está en el catálogo, y buena parte del personal de campo nunca
     * tendrá acceso al sistema. La columna es UNIQUE, así que dos cuentas no
     * pueden apuntar al mismo empleado.
     * </p>
     *
     * <p>
     * {@code nombres} y {@code apellidos} de arriba siguen siendo los de la
     * cuenta y no se derivan de aquí: hacerlo obligatorio rompería el
     * auto-registro y las semillas.
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_id")
    private Empleado empleado;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<UsuarioRol> usuarioRol = new ArrayList<>();
}
