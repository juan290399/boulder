package com.boulder.boulder.services;

import java.util.List;
import java.util.UUID;

import com.boulder.boulder.entities.Alteracion;
import com.boulder.boulder.entities.Estructural;
import com.boulder.boulder.entities.Litologia;
import com.boulder.boulder.entities.Mineralizacion;
import com.boulder.boulder.entities.Quicklog;

/**
 * Sincronización del logueo geológico con la aplicación de campo.
 *
 * <p>Cada plantilla necesita solo dos operaciones, y las cinco las mismas: bajar
 * lo que hay de un sondaje y subir un lote de filas. No hay un método por fila
 * porque el cliente trabaja sin cobertura y acumula el trabajo de una jornada
 * entera: cuando por fin hay red, manda de golpe lo pendiente.</p>
 *
 * <p><b>Las subidas son idempotentes.</b> El identificador lo genera el tablet
 * antes de tener red —dos geólogos en dos pozos distintos no pueden coordinarse
 * para no chocar— así que subir es "deja este registro con este contenido" y
 * repetir un envío que ya llegó no duplica nada. De ahí que el verbo sea PUT.</p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
public interface LogueoService {

    List<Litologia> listarLitologia(UUID sondajeId, boolean incluirEliminados);

    List<Alteracion> listarAlteracion(UUID sondajeId, boolean incluirEliminados);

    List<Mineralizacion> listarMineralizacion(UUID sondajeId, boolean incluirEliminados);

    List<Estructural> listarEstructural(UUID sondajeId, boolean incluirEliminados);

    List<Quicklog> listarQuicklog(UUID sondajeId, boolean incluirEliminados);

    int guardarLitologia(List<Litologia> items, String dispositivo, String usuario, String versionApp);

    int guardarAlteracion(List<Alteracion> items, String dispositivo, String usuario, String versionApp);

    int guardarMineralizacion(List<Mineralizacion> items, String dispositivo, String usuario, String versionApp);

    int guardarEstructural(List<Estructural> items, String dispositivo, String usuario, String versionApp);

    int guardarQuicklog(List<Quicklog> items, String dispositivo, String usuario, String versionApp);

    /** Última sincronización de cada tablet, la más reciente primero. */
    List<java.util.Map<String, Object>> dispositivos();
}
