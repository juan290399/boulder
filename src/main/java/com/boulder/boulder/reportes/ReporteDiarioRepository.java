package com.boulder.boulder.reportes;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.boulder.boulder.reportes.ReporteDiario.FilaSondaje;

/**
 * Consultas del reporte diario (patrón DAO con JdbcTemplate).
 *
 * <p>Agrega cuatro partes distintos por sondaje en una sola consulta: perforación,
 * logueo geológico, recepción de cajas y muestreo. Un sondaje aparece si tuvo
 * actividad en cualquiera de ellos. Todos los valores van como parámetros.</p>
 */
@Repository
public class ReporteDiarioRepository {

    private static final String POR_SONDAJE = """
            WITH perf AS (SELECT sondaje_id, sum(avance) AS m FROM operacional.prf_avance
                           WHERE fecha = ? GROUP BY sondaje_id),
                 logu AS (SELECT sondaje_id, sum(avance) AS m FROM operacional.log_avance
                           WHERE fecha = ? AND tipo_logueo = 'geologico' GROUP BY sondaje_id),
                 caja AS (SELECT sondaje_id, sum(total_cajas) AS n FROM operacional.log_recepcion_caja
                           WHERE fecha = ? GROUP BY sondaje_id),
                 mues AS (SELECT sondaje_id, sum(nro_muestras) AS n FROM operacional.mst_avance
                           WHERE fecha = ? GROUP BY sondaje_id),
                 ids  AS (SELECT sondaje_id FROM perf UNION SELECT sondaje_id FROM logu
                          UNION SELECT sondaje_id FROM caja UNION SELECT sondaje_id FROM mues)
            SELECT s.codigo,
                   coalesce(perf.m, 0) AS perforado, coalesce(logu.m, 0) AS logueado,
                   coalesce(caja.n, 0) AS cajas,     coalesce(mues.n, 0) AS muestras
              FROM ids
              JOIN operacional.prf_sondaje s ON s.id = ids.sondaje_id
              LEFT JOIN perf ON perf.sondaje_id = ids.sondaje_id
              LEFT JOIN logu ON logu.sondaje_id = ids.sondaje_id
              LEFT JOIN caja ON caja.sondaje_id = ids.sondaje_id
              LEFT JOIN mues ON mues.sondaje_id = ids.sondaje_id
            """;

    private final JdbcTemplate jdbc;

    public ReporteDiarioRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * @param fecha día consultado
     * @return actividad por sondaje en el día, sin orden garantizado
     */
    public List<FilaSondaje> porSondaje(LocalDate fecha) {
        Date dia = Date.valueOf(fecha);
        return jdbc.query(POR_SONDAJE, (rs, i) -> new FilaSondaje(
                rs.getString("codigo"),
                rs.getBigDecimal("perforado"),
                rs.getBigDecimal("logueado"),
                rs.getInt("cajas"),
                rs.getInt("muestras")), dia, dia, dia, dia);
    }

    /**
     * @param desde primer día, incluido
     * @param hasta último día, incluido
     * @return metros perforados por día; los días sin partes no aparecen
     */
    public Map<LocalDate, BigDecimal> perforadoPorDia(LocalDate desde, LocalDate hasta) {
        Map<LocalDate, BigDecimal> r = new LinkedHashMap<>();
        jdbc.query("SELECT fecha, sum(avance) AS m FROM operacional.prf_avance "
                        + "WHERE fecha BETWEEN ? AND ? GROUP BY fecha ORDER BY fecha",
                rs -> {
                    r.put(rs.getDate("fecha").toLocalDate(), rs.getBigDecimal("m"));
                },
                Date.valueOf(desde), Date.valueOf(hasta));
        return r;
    }
}
