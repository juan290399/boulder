package com.boulder.boulder.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import com.boulder.boulder.entities.Sondaje;

/**
 * Consulta del frente de avance: hasta qué profundidad llegó cada etapa del
 * flujo en cada sondaje.
 *
 * <p>
 * El dato que hace falta no es cuánto se hizo, sino <em>hasta dónde</em>. Un
 * sondaje perforado a 94 m con el logueo en 75 y el muestreo en 58 tiene dos
 * frentes atrasados, y esa distancia es la que el geólogo necesita ver de un
 * vistazo para saber de qué está colgado hoy.
 * </p>
 *
 * <p>
 * Se resuelve con una sola consulta nativa y subconsultas correlacionadas en
 * vez de nueve viajes a la base. Sale en SQL y no en JPQL porque hay que
 * agregar sobre siete tablas que no tienen relación entre sí, solo con el
 * sondaje: en JPQL eso obliga a siete consultas sueltas o a un producto
 * cartesiano entre agregados que devuelve sumas infladas.
 * </p>
 *
 * <p>
 * Extiende {@link Repository} y no {@code JpaRepository} porque no es un
 * repositorio de escritura: expone una lectura y nada más.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
public interface FrenteAvanceRepository extends Repository<Sondaje, UUID> {

    /**
     * Devuelve un renglón por sondaje con la profundidad alcanzada por cada
     * etapa, ordenado por actividad más reciente.
     *
     * <p>
     * El logueo sale como tres frentes separados —geológico, geomecánico y
     * estructural— porque así lo lleva el reporte diario y porque avanzan a
     * ritmos distintos sobre el mismo testigo: mezclarlos daría un frente que
     * no corresponde a ninguno. El cuarto tipo, {@code rapido}, es el quicklog
     * y no entra: no describe el testigo, lo adelanta.
     * </p>
     *
     * <p>
     * La fotografía sale de dos fuentes: el parte de fotografía
     * ({@code fot_avance}), que se reporta a mano, y las fotos cargadas
     * ({@code fot_foto}), que traen su propio tramo. Se toma la mayor: en
     * cuanto las fotos se suban de verdad, el parte manual deja de hacer falta.
     * {@code GREATEST} ignora los nulos, así que basta con que haya una de las dos.
     * </p>
     *
     * <p>
     * En la base el tipo se llama {@code geotecnico} y en el reporte
     * «Geomecánico». Se conserva el valor almacenado para no romper el CHECK
     * de {@code log_avance} ni la aplicación móvil, y se traduce la etiqueta
     * en la capa de presentación.
     * </p>
     */
    @Query(value = """
            SELECT s.id                  AS "sondajeId",
                   s.codigo              AS "sondajeCodigo",
                   s.nombre              AS "sondajeNombre",
                   s.profundidad_actual  AS "profundidadActual",
                   s.profundidad_final   AS "profundidadFinal",
                   s.fecha_inicio        AS "fechaInicio",
                   s.fecha_fin           AS "fechaFin",
                   (SELECT max(a.hasta) FROM operacional.prf_avance a
                     WHERE a.sondaje_id = s.id)                        AS "perforado",
                   (SELECT max(r.marcado_hasta) FROM operacional.log_recepcion_caja r
                     WHERE r.sondaje_id = s.id)                        AS "recepcionado",
                   (SELECT max(l.hasta) FROM operacional.log_avance l
                     WHERE l.sondaje_id = s.id AND l.tipo_logueo = 'geologico')   AS "logueado",
                   (SELECT max(l.hasta) FROM operacional.log_avance l
                     WHERE l.sondaje_id = s.id AND l.tipo_logueo = 'geotecnico')  AS "logueadoGeomecanico",
                   (SELECT max(l.hasta) FROM operacional.log_avance l
                     WHERE l.sondaje_id = s.id AND l.tipo_logueo = 'estructural') AS "logueadoEstructural",
                   GREATEST(
                       (SELECT max(f.marcado_hasta) FROM operacional.fot_avance f
                         WHERE f.sondaje_id = s.id),
                       (SELECT max(ff.hasta) FROM operacional.fot_foto ff
                         WHERE ff.sondaje_id = s.id AND ff.eliminado_en IS NULL)
                   )                                                   AS "fotografiado",
                   (SELECT max(c.hasta) FROM operacional.crt_muestra c
                     WHERE c.sondaje_id = s.id)                        AS "cortado",
                   (SELECT max(m.hasta) FROM operacional.mst_avance m
                     WHERE m.sondaje_id = s.id)                        AS "muestreado",
                   (SELECT max(d.hasta) FROM operacional.crt_densidad d
                     WHERE d.sondaje_id = s.id)                        AS "densidadCortada",
                   (SELECT coalesce(sum(m.nro_muestras_embolsadas), 0) FROM operacional.mst_avance m
                     WHERE m.sondaje_id = s.id)                        AS "muestrasEmbolsadas",
                   (SELECT coalesce(sum(r.total_cajas), 0) FROM operacional.log_recepcion_caja r
                     WHERE r.sondaje_id = s.id)                        AS "cajasRecibidas",
                   (SELECT max(t.f) FROM (
                        SELECT max(fecha) AS f FROM operacional.prf_avance         WHERE sondaje_id = s.id
                        UNION ALL SELECT max(fecha) FROM operacional.log_recepcion_caja WHERE sondaje_id = s.id
                        UNION ALL SELECT max(fecha) FROM operacional.log_avance         WHERE sondaje_id = s.id
                        UNION ALL SELECT max(fecha) FROM operacional.fot_avance         WHERE sondaje_id = s.id
                        UNION ALL SELECT max(fecha) FROM operacional.crt_muestra        WHERE sondaje_id = s.id
                        UNION ALL SELECT max(fecha) FROM operacional.mst_avance         WHERE sondaje_id = s.id
                        UNION ALL SELECT max(fecha) FROM operacional.crt_densidad       WHERE sondaje_id = s.id
                   ) t)                                                AS "ultimaActividad"
              FROM operacional.prf_sondaje s
             ORDER BY "ultimaActividad" DESC NULLS LAST, s.codigo
            """, nativeQuery = true)
    List<FrenteAvanceProjection> consultarFrente();
}
