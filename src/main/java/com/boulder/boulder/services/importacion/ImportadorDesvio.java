package com.boulder.boulder.services.importacion;

import java.math.BigDecimal;
import java.util.UUID;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boulder.boulder.dto.importacion.ResultadoImportacionDTO;
import com.boulder.boulder.entities.Empleado;
import com.boulder.boulder.entities.Maquina;
import com.boulder.boulder.entities.Sondaje;
import com.boulder.boulder.entities.Survey;
import com.boulder.boulder.entities.SurveyMedicion;
import com.boulder.boulder.repositories.SurveyMedicionRepository;
import com.boulder.boulder.repositories.SurveyRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Carga la desviación de sondajes: las estaciones de la trayectoria y el
 * evento de medición con su seguimiento.
 *
 * <p>
 * Son dos cosas distintas y por eso van en dos hojas. Una estación es un punto
 * medido del pozo —profundidad, azimut, inclinación— y hay cientos por sondaje;
 * con ellas se dibuja la trayectoria. Una medición es el hecho de haber
 * revisado la desviación en una fecha, con quién perforaba, qué se interpretó y
 * qué se mandó hacer. Las estaciones dicen por dónde va el pozo; las mediciones
 * dicen qué se decidió al respecto.
 * </p>
 *
 * <p>
 * La hoja de estaciones es la que hoy está vacía en veintiocho sondajes de los
 * treinta y uno que tienen desviación registrada, y es la razón por la que el
 * gráfico de trayectoria aparece en blanco en casi todos.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Component
public class ImportadorDesvio extends ImportadorBase {

    @Autowired
    private SurveyRepository estaciones;
    @Autowired
    private SurveyMedicionRepository mediciones;

    @PersistenceContext
    private EntityManager em;

    @Override
    public String clave() {
        return "desvio";
    }

    @Override
    public String archivoPlantilla() {
        return "PLANTILLA_desvio.xlsx";
    }

    @Override
    public String descripcion() {
        return "Estaciones de trayectoria y mediciones de desviación con su seguimiento.";
    }

    @Override
    protected void procesar(Workbook libro, Catalogos catalogos, boolean aplicar,
            ResultadoImportacionDTO resultado) {
        estaciones(libro, catalogos, aplicar, resultado);
        mediciones(libro, catalogos, aplicar, resultado);
    }

    private void estaciones(Workbook libro, Catalogos cat, boolean aplicar, ResultadoImportacionDTO r) {
        String sql = "SELECT sondaje_id::text || '|' || " + sqlNum("profundidad")
                + " FROM operacional.prf_survey";

        hoja(libro, "ESTACIONES", "operacional.prf_survey", sql, cat, aplicar, r, f -> {
            UUID sondajeId = f.sondajeReq("sondaje_codigo");
            Double profundidad = f.dobleReq("profundidad");
            Double azimut = f.dobleReq("azimut");
            Double inclinacion = f.dobleReq("inclinacion");

            f.referencia(f.texto("sondaje_codigo") + " @ " + f.texto("profundidad") + " m");

            if (azimut != null && (azimut < 0 || azimut > 360)) {
                f.error("azimut: tiene que estar entre 0 y 360 grados");
            }
            // La inclinación se registra negativa hacia abajo, que es la
            // convención del survey; se acepta el rango completo por si algún
            // pozo sube.
            if (inclinacion != null && (inclinacion < -90 || inclinacion > 90)) {
                f.error("inclinacion: tiene que estar entre -90 y 90 grados");
            }
            if (profundidad != null && profundidad < 0) {
                f.error("profundidad no puede ser negativa");
            }
            if (f.hayErrores()) {
                return null;
            }

            Survey e = new Survey();
            e.setSondaje(em.getReference(Sondaje.class, sondajeId));
            e.setProfundidad(profundidad);
            e.setAzimut(azimut);
            e.setInclinacion(inclinacion);
            e.setMetodoMedicion(f.texto("metodo_medicion", 50));
            e.setQc(f.texto("qc", 10));

            return new Registro(clave(sondajeId, profundidad), () -> estaciones.saveAndFlush(e));
        });
    }

    private void mediciones(Workbook libro, Catalogos cat, boolean aplicar, ResultadoImportacionDTO r) {
        String sql = "SELECT sondaje_id::text || '|' || " + sqlNum("profundidad") + " || '|' || "
                + sqlTexto("nro_medicion") + " FROM operacional.prf_survey_medicion";

        hoja(libro, "MEDICION", "operacional.prf_survey_medicion", sql, cat, aplicar, r, f -> {
            UUID sondajeId = f.sondajeReq("sondaje_codigo");
            BigDecimal profundidad = f.decimalReq("profundidad");
            Short nroMedicion = f.corto("nro_medicion");

            f.referencia(f.texto("sondaje_codigo") + " @ " + f.texto("profundidad") + " m");

            SurveyMedicion e = new SurveyMedicion();
            e.setFechaMedicion(f.fechaReq("fecha_medicion"));
            e.setProfundidad(profundidad);
            e.setNroMedicion(nroMedicion);
            e.setMetodo(f.texto("metodo", 50));
            e.setInstrumento(f.texto("instrumento", 50));
            e.setAzimutPlan(f.doble("azimut_plan"));
            e.setInclinacionPlan(f.doble("inclinacion_plan"));
            e.setAzimutMedido(f.doble("azimut_medido"));
            e.setInclinacionMedida(f.doble("inclinacion_medida"));
            e.setDesvAzimut(f.doble("desv_azimut"));
            e.setDesvAzimutSentido(f.opcion("desv_azimut_sentido", false, "izquierda", "derecha"));
            e.setDesvInclinacion(f.doble("desv_inclinacion"));
            e.setDesvInclinacionSentido(f.opcion("desv_inclinacion_sentido", false, "arriba", "abajo"));
            e.setDesvMetros(f.doble("desv_metros"));
            e.setDesvTotal(f.doble("desv_total"));
            e.setDecision(f.opcion("decision", false, "continuar", "corregir", "evaluar"));
            e.setInterpretacion(f.texto("interpretacion"));
            e.setInstruccion(f.texto("instruccion"));
            e.setResponsableSeguimiento(f.texto("responsable_seguimiento", 80));
            e.setEsPlanificada(Boolean.FALSE);

            // El perforista viene como nombre suelto. Se guarda siempre el
            // texto en responsable —es lo que dice el parte— y además se
            // resuelve la relación cuando la persona está dada de alta.
            String perforista = f.texto("perforista", 80);
            e.setResponsable(perforista);

            UUID maquinaId = f.maquina("maquina_codigo");

            // Una instrucción sin seguimiento declarado nace pendiente: si hay
            // algo que hacer, el estado por defecto no puede ser «sin
            // seguimiento», que es justamente lo que hace que se pierda.
            String estado = f.opcion("estado_seguimiento", false,
                    "sin_seguimiento", "pendiente", "en_curso", "cerrado");
            if (estado == null) {
                estado = e.getInstruccion() != null && !e.getInstruccion().isBlank()
                        ? "pendiente"
                        : "sin_seguimiento";
            }
            e.setEstadoSeguimiento(estado);

            if (e.getAzimutMedido() != null && (e.getAzimutMedido() < 0 || e.getAzimutMedido() > 360)) {
                f.error("azimut_medido: tiene que estar entre 0 y 360 grados");
            }
            if (profundidad != null && profundidad.signum() < 0) {
                f.error("profundidad no puede ser negativa");
            }
            if (f.hayErrores()) {
                return null;
            }

            e.setSondaje(em.getReference(Sondaje.class, sondajeId));
            if (maquinaId != null) {
                e.setMaquina(em.getReference(Maquina.class, maquinaId));
            }
            if (perforista != null) {
                UUID empleadoId = f.empleado(perforista);
                if (empleadoId != null) {
                    e.setPerforista(em.getReference(Empleado.class, empleadoId));
                }
            }

            return new Registro(clave(sondajeId, profundidad, nroMedicion),
                    () -> mediciones.saveAndFlush(e));
        });
    }
}
