package com.boulder.boulder.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boulder.boulder.dto.EtapaAvanceDTO;
import com.boulder.boulder.dto.FrenteAvanceDTO;
import com.boulder.boulder.repositories.FrenteAvanceProjection;
import com.boulder.boulder.repositories.FrenteAvanceRepository;

/**
 * Implementación de la consulta del frente de avance.
 *
 * <p>
 * La base entrega profundidades sueltas; aquí se convierten en las nueve
 * etapas ordenadas, con su rezago, y se señala cuál es la crítica.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Service
public class FrenteAvanceServiceImpl implements FrenteAvanceService {

    private static final Logger log = LoggerFactory.getLogger(FrenteAvanceServiceImpl.class);

    /**
     * Orden real del flujo de testigo. La secuencia importa: una etapa no puede
     * adelantar a la anterior, y la pantalla la dibuja en este orden.
     *
     * <p>
     * El logueo son tres etapas y no una. Geológico, geomecánico y estructural
     * describen el mismo testigo pero avanzan por separado —el reporte diario
     * los lleva en tres columnas— y un sondaje puede tener el geológico al día
     * y el estructural cien metros atrás. Con una sola etapa ese atraso
     * quedaba invisible.
     * </p>
     *
     * <p>
     * La clave {@code geomecanico} corresponde al tipo {@code geotecnico} de
     * {@code log_avance}: se conserva el valor almacenado y se traduce la
     * etiqueta, que es como lo nombra el reporte.
     * </p>
     */
    private static final String[][] ETAPAS = {
            { "perforacion",  "Perforación" },
            { "recepcion",    "Recepción de cajas" },
            { "logueo",       "Logueo geológico" },
            { "geomecanico",  "Logueo geomecánico" },
            { "estructural",  "Logueo estructural" },
            { "fotografia",   "Fotografía" },
            { "corte",        "Corte geoquímico" },
            { "muestreo",     "Muestreo" },
            { "densidad",     "Corte de densidad" },
    };

    @Autowired
    private FrenteAvanceRepository frenteAvanceRepository;

    @Override
    @Transactional(readOnly = true)
    public List<FrenteAvanceDTO> consultar(boolean soloActivos) {
        log.debug("Consultando el frente de avance. soloActivos={}", soloActivos);

        List<FrenteAvanceDTO> resultado = new ArrayList<>();
        for (FrenteAvanceProjection p : frenteAvanceRepository.consultarFrente()) {
            boolean activo = p.getFechaFin() == null;
            if (soloActivos && !activo) {
                continue;
            }
            resultado.add(construir(p, activo));
        }
        return resultado;
    }

    private FrenteAvanceDTO construir(FrenteAvanceProjection p, boolean activo) {
        FrenteAvanceDTO dto = new FrenteAvanceDTO();
        dto.setSondajeId(p.getSondajeId());
        dto.setSondajeCodigo(p.getSondajeCodigo());
        dto.setSondajeNombre(p.getSondajeNombre());
        dto.setProfundidadActual(p.getProfundidadActual());
        dto.setProfundidadFinal(p.getProfundidadFinal());
        dto.setFechaInicio(p.getFechaInicio());
        dto.setFechaFin(p.getFechaFin());
        dto.setUltimaActividad(p.getUltimaActividad());
        dto.setActivo(activo);
        dto.setMuestrasEmbolsadas(p.getMuestrasEmbolsadas());
        dto.setCajasRecibidas(p.getCajasRecibidas());

        if (p.getUltimaActividad() != null) {
            dto.setDiasSinMovimiento(ChronoUnit.DAYS.between(p.getUltimaActividad(), LocalDate.now()));
        }

        // La referencia contra la que se mide el rezago es lo perforado. Si no
        // hay ningún parte de perforación se cae a la profundidad que declara el
        // sondaje: sin referencia, todas las etapas parecerían al día.
        BigDecimal referencia = p.getPerforado() != null ? p.getPerforado() : p.getProfundidadActual();

        // La etapa de perforacion muestra la misma referencia, no el agregado de
        // partes: los 72 sondajes del historico se cargaron antes de que existiera
        // el parte diario, y sin esto aparecerian como si la perforacion llevara
        // cientos de metros de atraso respecto de si misma.
        BigDecimal[] valores = {
                referencia,
                p.getRecepcionado(),
                p.getLogueado(),
                p.getLogueadoGeomecanico(),
                p.getLogueadoEstructural(),
                p.getFotografiado(),
                p.getCortado(),
                p.getMuestreado(),
                p.getDensidadCortada(),
        };

        String etapaCritica = null;
        BigDecimal rezagoMaximo = BigDecimal.ZERO;

        for (int i = 0; i < ETAPAS.length; i++) {
            EtapaAvanceDTO etapa = new EtapaAvanceDTO();
            etapa.setClave(ETAPAS[i][0]);
            etapa.setNombre(ETAPAS[i][1]);
            etapa.setProfundidad(valores[i]);

            if (referencia != null && referencia.signum() > 0) {
                // Una etapa sin ningún parte cuenta como cero, no como ausente:
                // que nunca se haya fotografiado un sondaje perforado a 300 m es
                // precisamente el atraso que hay que ver.
                BigDecimal alcanzado = valores[i] != null ? valores[i] : BigDecimal.ZERO;
                BigDecimal rezago = referencia.subtract(alcanzado).max(BigDecimal.ZERO);

                etapa.setRezago(rezago);
                etapa.setPctSobrePerforado(alcanzado
                        .multiply(BigDecimal.valueOf(100))
                        .divide(referencia, 1, RoundingMode.HALF_UP)
                        .min(BigDecimal.valueOf(100)));

                if (i > 0 && rezago.compareTo(rezagoMaximo) > 0) {
                    rezagoMaximo = rezago;
                    etapaCritica = ETAPAS[i][0];
                }
            }
            dto.getEtapas().add(etapa);
        }

        dto.setEtapaCritica(etapaCritica);
        dto.setRezagoMaximo(rezagoMaximo);

        if (p.getProfundidadFinal() != null && p.getProfundidadFinal().signum() > 0 && referencia != null) {
            dto.setPctProgramado(referencia
                    .multiply(BigDecimal.valueOf(100))
                    .divide(p.getProfundidadFinal(), 1, RoundingMode.HALF_UP));
        }

        return dto;
    }
}
