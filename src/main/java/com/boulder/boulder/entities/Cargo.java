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
 * Puesto laboral de un empleado dentro de la operación.
 *
 * <p>
 * No confundir con {@link Rol}, que es un permiso del sistema. El rol decide
 * qué puede hacer alguien en la aplicación; el cargo dice quién es en la
 * operación. Un perforista tiene cargo y normalmente no tiene cuenta de
 * acceso, y un administrador de sistemas tiene rol sin ser personal de campo.
 * </p>
 *
 * <p>Se persiste en la tabla {@code base.cargo}.</p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "cargo", schema = "base")
@Data
public class Cargo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "numero_interno", insertable = false, updatable = false)
    private Integer numeroInterno;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(nullable = false, length = 60)
    private String nombre;

    /**
     * Área a la que pertenece el puesto: {@code perforacion}, {@code geologia},
     * {@code laboratorio} o {@code administracion}. Permite ofrecer en cada
     * formulario solo los empleados que tienen sentido: perforistas en el parte
     * de perforación, geólogos en el de logueo.
     */
    @Column(nullable = false, length = 20)
    private String area;

    @Column(nullable = false)
    private Boolean estado = Boolean.TRUE;
}
