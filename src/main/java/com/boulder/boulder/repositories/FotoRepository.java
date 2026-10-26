package com.boulder.boulder.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.boulder.boulder.entities.Foto;

public interface FotoRepository extends JpaRepository<Foto, UUID> {

    /** Las fotos vigentes de un sondaje, en el orden en que se lee el testigo. */
    @Query("""
            SELECT f FROM Foto f
             WHERE f.sondajeId = :sondajeId AND f.eliminadoEn IS NULL
             ORDER BY f.desde ASC NULLS LAST, f.cajaDesde ASC NULLS LAST, f.creadoEn ASC
            """)
    List<Foto> vigentesDe(UUID sondajeId);
}
