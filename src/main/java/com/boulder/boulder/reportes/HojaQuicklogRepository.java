package com.boulder.boulder.reportes;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.boulder.boulder.reportes.HojaQuicklog.Cabecera;
import com.boulder.boulder.reportes.HojaQuicklog.Tramo;

/** Consultas de la hoja de registro de quicklog (patrón DAO con JdbcTemplate). */
@Repository
public class HojaQuicklogRepository {

    private static final String CABECERA = """
            SELECT s.codigo, p.nombre AS ubicacion, m.codigo AS maquina,
                   coalesce(s.azimut_setup, ps.azimut) AS azimut,
                   coalesce(s.inclinacion_setup, ps.inclinacion) AS dip,
                   c.norte, c.este, c.elevacion,
                   coalesce(ps.profundidad, s.profundidad_final) AS profundidad
              FROM operacional.prf_sondaje s
              LEFT JOIN base.proyecto p ON p.id = s.proyecto_id
              LEFT JOIN operacional.prf_maquina m ON m.id = s.maquina_id
              LEFT JOIN operacional.prf_collar c ON c.id = s.collar_id
              LEFT JOIN operacional.prf_programa_sondaje ps ON ps.id = s.programa_id
             WHERE s.id = ?
            """;

    private static final String TRAMOS = """
            SELECT q.fecha, q.desde, q.hasta, q.longitud, lit.codigo AS lito,
                   nullif(concat_ws('-', alt1.codigo, alt2.codigo), '') AS alteracion,
                   nullif(concat_ws('-', aso1.codigo, aso2.codigo, aso3.codigo), '') AS ensamble,
                   nullif(concat_ws('-', min1.codigo, min2.codigo, min3.codigo), '') AS mineralizacion,
                   q.pc_py, q.pc_cu, q.pc_mo,
                   nullif(concat_ws('-', est1.codigo, est2.codigo, est3.codigo), '') AS estilo,
                   zona.codigo AS zona, q.comentarios, q.geologo
              FROM logueo.iquicklog q
              LEFT JOIN geologia.lit_diccionario lit ON lit.id = q.lito_id
              LEFT JOIN geologia.alt_diccionario alt1 ON alt1.id = q.alt_tipo_1_id
              LEFT JOIN geologia.alt_diccionario alt2 ON alt2.id = q.alt_tipo_2_id
              LEFT JOIN geologia.alt_mineral aso1 ON aso1.id = q.alt_aso_1_id
              LEFT JOIN geologia.alt_mineral aso2 ON aso2.id = q.alt_aso_2_id
              LEFT JOIN geologia.alt_mineral aso3 ON aso3.id = q.alt_aso_3_id
              LEFT JOIN geologia.min_diccionario min1 ON min1.id = q.min_tipo_1_id
              LEFT JOIN geologia.min_diccionario min2 ON min2.id = q.min_tipo_2_id
              LEFT JOIN geologia.min_diccionario min3 ON min3.id = q.min_tipo_3_id
              LEFT JOIN geologia.min_distribucion est1 ON est1.id = q.min_estilo_1_id
              LEFT JOIN geologia.min_distribucion est2 ON est2.id = q.min_estilo_2_id
              LEFT JOIN geologia.min_distribucion est3 ON est3.id = q.min_estilo_3_id
              LEFT JOIN geologia.min_zona zona ON zona.id = q.min_zona_id
             WHERE q.sondaje_id = ? AND q.eliminado_en IS NULL
            """;

    private final JdbcTemplate jdbc;

    public HojaQuicklogRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** @return la cabecera, o vacío si el sondaje no existe */
    public Optional<Cabecera> cabecera(UUID sondajeId) {
        return jdbc.query(CABECERA, (rs, i) -> new Cabecera(
                rs.getString("codigo"),
                rs.getString("ubicacion"),
                rs.getString("maquina"),
                rs.getObject("azimut", Double.class),
                rs.getObject("dip", Double.class),
                rs.getObject("norte", Double.class),
                rs.getObject("este", Double.class),
                rs.getObject("elevacion", Double.class),
                rs.getObject("profundidad", Double.class)), sondajeId).stream().findFirst();
    }

    /** @return los tramos vigentes del sondaje, sin orden garantizado */
    public List<Tramo> tramos(UUID sondajeId) {
        return jdbc.query(TRAMOS, (rs, i) -> new Tramo(
                rs.getDate("fecha") != null ? rs.getDate("fecha").toLocalDate() : null,
                rs.getBigDecimal("desde"),
                rs.getBigDecimal("hasta"),
                rs.getBigDecimal("longitud"),
                rs.getString("lito"),
                rs.getString("alteracion"),
                rs.getString("ensamble"),
                rs.getString("mineralizacion"),
                rs.getBigDecimal("pc_py"),
                rs.getBigDecimal("pc_cu"),
                rs.getBigDecimal("pc_mo"),
                rs.getString("estilo"),
                rs.getString("zona"),
                rs.getString("comentarios"),
                rs.getString("geologo")), sondajeId);
    }
}
