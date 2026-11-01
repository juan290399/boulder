package com.boulder.boulder.services.importacion;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;

import com.boulder.boulder.dominio.CodigoSondaje;

/**
 * Traduce los códigos que vienen en la plantilla al identificador interno.
 *
 * <p>
 * Es la pieza que impide que vuelva a entrar el desorden que hay hoy en los
 * reportes: mientras un sondaje se pueda escribir «Sondaje 036», «DDH-35» o
 * «VERO S 38_26» y el sistema lo acepte, la base termina con tres frentes
 * distintos para el mismo pozo. Acá un código o resuelve contra el catálogo o
 * el renglón se rechaza.
 * </p>
 *
 * <p>
 * Cada catálogo se lee una sola vez y se guarda en memoria durante la
 * importación. Son unos pocos cientos de filas en total y se evitan miles de
 * viajes a la base cuando la plantilla trae dos mil renglones.
 * </p>
 *
 * <p>
 * La comparación es sin distinguir mayúsculas y sin espacios en los bordes.
 * Para sondajes, si el código no aparece tal cual se prueba su forma canónica
 * ({@link CodigoSondaje}): «VERO S 35_26» resuelve a {@code DDH-VERO-S-035-26};
 * una escritura ambigua como «DDH-35» no resuelve.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
public class Catalogos {

    /** Código reservado para los partes que no corresponden a un sondaje. */
    public static final String SIN_SONDAJE = "VARIOS";

    private static final String SONDAJES = "operacional.prf_sondaje";

    private final JdbcTemplate jdbc;
    private final Map<String, Map<String, UUID>> cache = new HashMap<>();

    public Catalogos(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public UUID sondaje(String codigo) {
        UUID id = buscar(SONDAJES, "codigo", codigo);
        if (id == null && codigo != null) {
            id = CodigoSondaje.normalizar(codigo).map(c -> buscar(SONDAJES, "codigo", c)).orElse(null);
        }
        return id;
    }

    /** Código del catálogo de un sondaje ya resuelto. */
    public String codigoSondaje(UUID id) {
        if (id == null) {
            return null;
        }
        return mapa(SONDAJES, "codigo").entrySet().stream()
                .filter(e -> e.getValue().equals(id)).map(Map.Entry::getKey).findFirst().orElse(null);
    }

    public UUID maquina(String codigo) {
        return buscar("operacional.prf_maquina", "codigo", codigo);
    }

    public UUID plataforma(String codigo) {
        return buscar("operacional.prf_plataforma", "codigo", codigo);
    }

    /**
     * Busca a una persona por su nombre completo, que es como la nombran los
     * reportes. No se rechaza el renglón cuando no aparece: el parte de turno
     * lista la dotación en texto libre y muchos nombres no están dados de alta.
     * El nombre se conserva igual y la relación queda sin resolver.
     */
    public UUID empleado(String nombreCompleto) {
        return buscar("base.empleado", "nombres || ' ' || apellidos", nombreCompleto);
    }

    /** Resuelve un código contra uno de los diccionarios de geología. */
    public UUID geologia(String tabla, String codigo) {
        return buscar("geologia." + tabla, "codigo", codigo);
    }

    /** Códigos válidos de un catálogo, para poder mostrarlos en el error. */
    public List<String> codigos(String tabla) {
        return jdbc.queryForList("SELECT codigo FROM " + tabla + " ORDER BY codigo", String.class);
    }

    private UUID buscar(String tabla, String expresionCodigo, String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return mapa(tabla, expresionCodigo).get(normalizar(valor));
    }

    private Map<String, UUID> mapa(String tabla, String expresionCodigo) {
        return cache.computeIfAbsent(tabla, t -> {
            Map<String, UUID> m = new LinkedHashMap<>();
            jdbc.query("SELECT id, " + expresionCodigo + " AS clave FROM " + t,
                    rs -> {
                        String clave = rs.getString("clave");
                        if (clave != null) {
                            m.putIfAbsent(normalizar(clave), (UUID) rs.getObject("id"));
                        }
                    });
            return m;
        });
    }

    private static String normalizar(String s) {
        return s.trim().toUpperCase();
    }
}
