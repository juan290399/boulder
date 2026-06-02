package com.boulder.boulder.entities;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProyectoMaquinaId implements Serializable {

    @Column(name = "proyecto_id")
    private UUID proyectoId;

    @Column(name = "maquina_id")
    private UUID maquinaId;

    @Column(name = "fecha_ingreso")
    private LocalDate fechaIngreso;

    // Implementación obligatoria de equals y hashCode para llaves compuestas en JPA
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProyectoMaquinaId that = (ProyectoMaquinaId) o;
        return Objects.equals(proyectoId, that.proyectoId) && 
               Objects.equals(maquinaId, that.maquinaId) && 
               Objects.equals(fechaIngreso, that.fechaIngreso);
    }

    @Override
    public int hashCode() {
        return Objects.hash(proyectoId, maquinaId, fechaIngreso);
    }
}