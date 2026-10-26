package com.boulder.boulder.repositories;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.boulder.boulder.dto.CatalogoGeologiaDTO;

/**
 * Lee de una vez los veintiún diccionarios del esquema {@code geologia}.
 *
 * <p><b>Por qué no son veintiuna entidades JPA.</b> Serían veintiuna clases,
 * veintiún repositorios y veintiún servicios para describir lo mismo: cuatro
 * columnas iguales y tres opcionales. El cliente las consume aplanadas en una
 * sola tabla, así que mapearlas una a una solo serviría para volver a juntarlas
 * después.</p>
 *
 * <p><b>Por qué la consulta se construye sola.</b> La lista de catálogos crece:
 * en septiembre de 2026 entraron seis de golpe (los {@code est_*}). Si estuviera
 * escrita a mano, añadir el vigésimo segundo obligaría a acordarse de tocar esto,
 * y el síntoma sería un botón vacío en el tablet, sin error en ningún log. Se
 * toma del propio catálogo de PostgreSQL: <em>toda tabla base de
 * {@code geologia} es un diccionario</em>, y de cada una se mira cuáles de las
 * tres columnas opcionales tiene.</p>
 *
 * <p>Las vistas quedan fuera por el filtro {@code BASE TABLE}: {@code geologia}
 * tiene además tres {@code vw_mm_paleta_*} para exportar a Micromine, que no son
 * catálogos.</p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Repository
public class CatalogoGeologiaRepository {

    private static final String ESQUEMA = "geologia";

    /** Columnas obligatorias, las que definen lo que es un catálogo. */
    private static final String COLUMNAS_BASE = "id, numero_interno, codigo, nombre";

    @Autowired
    private JdbcTemplate jdbc;

    public List<CatalogoGeologiaDTO> listarTodos() {
        String sql = construirConsulta(inventariarColumnas());

        return jdbc.query(sql, (rs, fila) -> {
            CatalogoGeologiaDTO dto = new CatalogoGeologiaDTO();
            dto.setId((java.util.UUID) rs.getObject("id"));
            dto.setTipo(rs.getString("tipo"));
            dto.setCodigo(rs.getString("codigo"));
            dto.setNombre(rs.getString("nombre"));
            dto.setGrupoId((java.util.UUID) rs.getObject("grupo_id"));
            dto.setColorR(leerShort(rs, "color_r"));
            dto.setColorG(leerShort(rs, "color_g"));
            dto.setColorB(leerShort(rs, "color_b"));
            dto.setNumeroInterno((Integer) rs.getObject("numero_interno"));
            dto.setEstaActivo(rs.getBoolean("esta_activo"));
            return dto;
        });
    }

    /** {@code null} y no cero: la ausencia de color hay que poder distinguirla del negro. */
    private Short leerShort(java.sql.ResultSet rs, String columna) throws java.sql.SQLException {
        short valor = rs.getShort(columna);
        return rs.wasNull() ? null : valor;
    }

    /** Qué columnas tiene de verdad cada tabla base de {@code geologia}. */
    private Map<String, Set<String>> inventariarColumnas() {
        Map<String, Set<String>> porTabla = new LinkedHashMap<>();

        jdbc.query(
            "SELECT c.table_name, c.column_name "
          + "  FROM information_schema.columns c "
          + "  JOIN information_schema.tables t "
          + "    ON t.table_schema = c.table_schema AND t.table_name = c.table_name "
          + " WHERE c.table_schema = ? AND t.table_type = 'BASE TABLE' "
          + " ORDER BY c.table_name",
            rs -> {
                porTabla.computeIfAbsent(rs.getString("table_name"), t -> new TreeSet<>())
                        .add(rs.getString("column_name"));
            },
            ESQUEMA);

        return porTabla;
    }

    private String construirConsulta(Map<String, Set<String>> porTabla) {
        if (porTabla.isEmpty()) {
            throw new IllegalStateException(
                "El esquema '" + ESQUEMA + "' no tiene ninguna tabla de catálogo.");
        }

        List<String> partes = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entrada : porTabla.entrySet()) {
            String tabla = entrada.getKey();
            Set<String> columnas = entrada.getValue();

            // Los nombres salen del catálogo de PostgreSQL, no de la petición,
            // asi que no hay nada que parametrizar aquí.
            partes.add(
                "SELECT " + COLUMNAS_BASE
              // `codigo` es character(N) en varias tablas y llega con relleno a
              // la derecha. El cliente lo pinta tal cual en el botón.
              + " , trim(codigo) AS codigo_limpio"
              + " , '" + tabla + "'::text AS tipo"
              + opcional(columnas, "grupo_id", "uuid")
              + opcional(columnas, "color_r", "smallint")
              + opcional(columnas, "color_g", "smallint")
              + opcional(columnas, "color_b", "smallint")
              + (columnas.contains("esta_activo")
                    ? " , esta_activo"
                    : " , true AS esta_activo")
              + " FROM " + ESQUEMA + "." + tabla);
        }

        // El SELECT exterior se queda con el `codigo` ya recortado y descarta el
        // original; hacerlo dentro obligaria a repetir el trim en cada rama.
        return "SELECT id, tipo, codigo_limpio AS codigo, nombre, grupo_id,"
             + " color_r, color_g, color_b, numero_interno, esta_activo"
             + " FROM (" + String.join(" UNION ALL ", partes) + ") c"
             + " ORDER BY tipo, numero_interno";
    }

    private String opcional(Set<String> columnas, String columna, String tipo) {
        return columnas.contains(columna)
            ? " , " + columna
            : " , NULL::" + tipo + " AS " + columna;
    }
}
