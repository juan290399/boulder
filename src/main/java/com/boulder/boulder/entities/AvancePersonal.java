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
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Integrante de la dotación de máquina asociado a un parte diario de perforación.
 *
 * <p>Se persiste en la tabla {@code operacional.prf_avance_personal}.</p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "prf_avance_personal", schema = "operacional")
@Data
public class AvancePersonal implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "avance_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private AvancePerforacion avance;

    /**
     * El perforista de ese turno. La columna conserva el nombre
     * {@code perforista_id} y su significado: no es "un empleado cualquiera",
     * es quien operaba la máquina. Lo que cambió es la tabla a la que apunta,
     * ahora {@code base.empleado}.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "perforista_id")
    private Empleado perforista;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(length = 40)
    private String cargo;

    @Column(name = "codigo_maquina", length = 20)
    private String codigoMaquina;
}
