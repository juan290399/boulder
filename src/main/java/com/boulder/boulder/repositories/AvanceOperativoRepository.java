package com.boulder.boulder.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import com.boulder.boulder.entities.AvanceOperativo;

/**
 * Consultas comunes a todos los partes de avance operativo (UC16 a UC20).
 *
 * <p>
 * Va anotado con {@code @NoRepositoryBean} para que Spring Data no intente
 * instanciarlo: no es un repositorio en sí, sino la plantilla de la que
 * heredan los siete concretos. Cada uno de ellos sustituye {@code E} por su
 * entidad y hereda estas tres búsquedas ya derivadas.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@NoRepositoryBean
public interface AvanceOperativoRepository<E extends AvanceOperativo> extends JpaRepository<E, UUID> {

    List<E> findBySondajeIdOrderByFechaAscNumeroInternoAsc(UUID sondajeId);

    List<E> findByFechaOrderByNumeroInternoAsc(LocalDate fecha);

    List<E> findByFechaBetweenOrderByFechaAscNumeroInternoAsc(LocalDate desde, LocalDate hasta);
}
