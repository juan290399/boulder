package com.boulder.boulder.services;

import java.util.List;
import java.util.UUID;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boulder.boulder.entities.Alteracion;
import com.boulder.boulder.entities.Estructural;
import com.boulder.boulder.entities.Litologia;
import com.boulder.boulder.entities.Mineralizacion;
import com.boulder.boulder.entities.Quicklog;
import com.boulder.boulder.entities.RegistroLogueo;
import com.boulder.boulder.eventos.CambioDatos;
import com.boulder.boulder.repositories.AlteracionRepository;
import com.boulder.boulder.repositories.EstructuralRepository;
import com.boulder.boulder.repositories.LitologiaRepository;
import com.boulder.boulder.repositories.MineralizacionRepository;
import com.boulder.boulder.repositories.QuicklogRepository;

/**
 * Implementación de la sincronización del logueo geológico.
 *
 * <p><b>Por qué aquí no hay DTO.</b> El resto del proyecto convierte entidad a
 * DTO para no dejar escapar proxies de Hibernate al JSON. Estas cinco entidades
 * no tienen ni una relación —las claves foráneas a {@code geologia} son UUID
 * sueltos, ver {@link Litologia}— así que ese riesgo no existe, y un DTO sería
 * una copia literal de hasta cuarenta campos por plantilla, con su conversor y
 * su ocasión de olvidarse de uno. Las columnas ya se llaman igual en los dos
 * extremos; esa coincidencia es justamente lo que hace que sincronizar sea
 * copiar y no traducir.</p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Service
public class LogueoServiceImpl implements LogueoService {

    private static final Logger log = LoggerFactory.getLogger(LogueoServiceImpl.class);

    @Autowired
    private LitologiaRepository litologiaRepository;

    @Autowired
    private AlteracionRepository alteracionRepository;

    @Autowired
    private MineralizacionRepository mineralizacionRepository;

    @Autowired
    private EstructuralRepository estructuralRepository;

    @Autowired
    private QuicklogRepository quicklogRepository;

    @Autowired
    private ApplicationEventPublisher eventos;

    @Autowired
    private JdbcTemplate jdbc;

    // ------------------------------------------------------------------
    // Bajada
    // ------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<Litologia> listarLitologia(UUID sondajeId, boolean incluirEliminados) {
        return registrar("litología", sondajeId,
                vigentes(litologiaRepository.findBySondajeIdOrderByDesdeAsc(sondajeId), incluirEliminados));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Alteracion> listarAlteracion(UUID sondajeId, boolean incluirEliminados) {
        return registrar("alteración", sondajeId,
                vigentes(alteracionRepository.findBySondajeIdOrderByDesdeAsc(sondajeId), incluirEliminados));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Mineralizacion> listarMineralizacion(UUID sondajeId, boolean incluirEliminados) {
        return registrar("mineralización", sondajeId,
                vigentes(mineralizacionRepository.findBySondajeIdOrderByDesdeAsc(sondajeId), incluirEliminados));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Estructural> listarEstructural(UUID sondajeId, boolean incluirEliminados) {
        return registrar("estructural", sondajeId,
                vigentes(estructuralRepository.findBySondajeIdOrderByProfundidadAsc(sondajeId), incluirEliminados));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Quicklog> listarQuicklog(UUID sondajeId, boolean incluirEliminados) {
        return registrar("quicklog", sondajeId,
                vigentes(quicklogRepository.findBySondajeIdOrderByDesdeAsc(sondajeId), incluirEliminados));
    }

    // ------------------------------------------------------------------
    // Subida
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public int guardarLitologia(List<Litologia> items, String dispositivo, String usuario, String versionApp) {
        return guardar(dispositivo, usuario, versionApp, "litología", items, litologiaRepository, Litologia::getId);
    }

    @Override
    @Transactional
    public int guardarAlteracion(List<Alteracion> items, String dispositivo, String usuario, String versionApp) {
        return guardar(dispositivo, usuario, versionApp, "alteración", items, alteracionRepository, Alteracion::getId);
    }

    @Override
    @Transactional
    public int guardarMineralizacion(List<Mineralizacion> items, String dispositivo, String usuario, String versionApp) {
        return guardar(dispositivo, usuario, versionApp, "mineralización", items, mineralizacionRepository, Mineralizacion::getId);
    }

    @Override
    @Transactional
    public int guardarEstructural(List<Estructural> items, String dispositivo, String usuario, String versionApp) {
        return guardar(dispositivo, usuario, versionApp, "estructural", items, estructuralRepository, Estructural::getId);
    }

    @Override
    @Transactional
    public int guardarQuicklog(List<Quicklog> items, String dispositivo, String usuario, String versionApp) {
        return guardar(dispositivo, usuario, versionApp, "quicklog", items, quicklogRepository, Quicklog::getId);
    }

    // ------------------------------------------------------------------

    private <T> List<T> registrar(String plantilla, UUID sondajeId, List<T> filas) {
        log.debug("Entregando {} filas de {} del sondaje {}", filas.size(), plantilla, sondajeId);
        return filas;
    }

    /**
     * Guarda un lote entero o ninguno.
     *
     * <p>{@code saveAll} sobre entidades con el id ya puesto hace un UPSERT:
     * Hibernate mira si la fila existe y actualiza o inserta. Es lo que permite
     * que el tablet reintente una subida sin duplicar nada cuando la cobertura
     * se corta a mitad.</p>
     *
     * <p>Se rechaza el lote sin id en vez de generarlo aquí: un id puesto por el
     * servidor no es el que el tablet tiene guardado, así que la fila se
     * duplicaría en el siguiente intento y nadie se enteraría.</p>
     */
    private <T extends RegistroLogueo> int guardar(String dispositivo, String usuario, String versionApp,
                            String plantilla,
                            List<T> items,
                            JpaRepository<T, UUID> repositorio,
                            Function<T, UUID> idDe) {

        if (items == null || items.isEmpty()) {
            log.debug("Subida de {} sin contenido; no hay nada que hacer", plantilla);
            return 0;
        }

        for (T item : items) {
            if (idDe.apply(item) == null) {
                log.warn("Subida de {} rechazada: llega una fila sin identificador", plantilla);
                throw new IllegalArgumentException(
                    "Cada registro de " + plantilla + " debe traer su propio id; "
                  + "lo genera el cliente para que la subida se pueda repetir sin duplicar.");
            }
        }

        // Primero las bajas y con flush. Hibernate ejecuta los INSERT antes que
        // los UPDATE al vaciar la sesión, así que si el tablet borró un tramo y
        // logueó otro en el mismo metro dentro de la misma subida, el nuevo
        // chocaría con la restricción de no solapamiento mientras el viejo
        // todavía figura como vigente.
        List<T> bajas = items.stream().filter(i -> i.getEliminadoEn() != null).toList();
        List<T> vigentes = items.stream().filter(i -> i.getEliminadoEn() == null).toList();
        if (!bajas.isEmpty()) {
            repositorio.saveAllAndFlush(bajas);
        }
        repositorio.saveAll(vigentes);
        log.info("Guardadas {} filas de {} ({} bajas)", items.size(), plantilla, bajas.size());

        Set<UUID> sondajes = items.stream().map(RegistroLogueo::getSondajeId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        eventos.publishEvent(CambioDatos.de(recursoDe(plantilla), sondajes,
                dispositivo != null ? "tablet" : "web", items.size()));
        registrarDispositivo(dispositivo, usuario, versionApp, items.size());
        return items.size();
    }

    /**
     * Quita las filas borradas, salvo que las pida el tablet.
     *
     * <p>El tablet sí las necesita: es como se entera de que otro dispositivo
     * borró un tramo que él todavía tiene. La web no: para ella, borrado es
     * borrado.</p>
     */
    private <T extends RegistroLogueo> List<T> vigentes(List<T> filas, boolean incluirEliminados) {
        return incluirEliminados ? filas
                : filas.stream().filter(f -> f.getEliminadoEn() == null).toList();
    }

    /** Nombre del recurso en los avisos: sin tildes, como las rutas del API. */
    private static String recursoDe(String plantilla) {
        return switch (plantilla) {
            case "litología" -> "litologia";
            case "alteración" -> "alteracion";
            case "mineralización" -> "mineralizacion";
            default -> plantilla;
        };
    }

    /**
     * Deja constancia de la subida de un tablet. Un fallo aquí no puede tumbar
     * la subida del logueo, que es lo que importa: se registra y se sigue.
     */
    private void registrarDispositivo(String dispositivo, String usuario, String versionApp, int filas) {
        if (dispositivo == null || dispositivo.isBlank()) {
            return;
        }
        try {
            jdbc.update("""
                    INSERT INTO logueo.dispositivo_sync
                        (dispositivo, usuario, version_app, ultimo_contacto, ultima_subida, filas_subidas)
                    VALUES (?, ?, ?, now(), now(), ?)
                    ON CONFLICT (dispositivo) DO UPDATE SET
                        usuario = EXCLUDED.usuario,
                        version_app = COALESCE(EXCLUDED.version_app, logueo.dispositivo_sync.version_app),
                        ultimo_contacto = now(),
                        ultima_subida = now(),
                        filas_subidas = logueo.dispositivo_sync.filas_subidas + EXCLUDED.filas_subidas
                    """, recortar(dispositivo, 64), recortar(usuario, 80), recortar(versionApp, 20), filas);
        } catch (RuntimeException e) {
            log.warn("No se pudo registrar la sincronización del dispositivo {}", dispositivo, e);
        }
    }

    private static String recortar(String s, int max) {
        return s == null ? null : (s.length() > max ? s.substring(0, max) : s);
    }

    @Override
    @Transactional(readOnly = true)
    public List<java.util.Map<String, Object>> dispositivos() {
        return jdbc.queryForList("""
                SELECT dispositivo, usuario, version_app AS "versionApp",
                       ultimo_contacto AS "ultimoContacto", ultima_subida AS "ultimaSubida",
                       filas_subidas AS "filasSubidas"
                  FROM logueo.dispositivo_sync
                 ORDER BY ultimo_contacto DESC
                """);
    }
}
