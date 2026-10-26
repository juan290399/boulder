package com.boulder.boulder.services.importacion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boulder.boulder.dominio.ParteTurno;
import com.boulder.boulder.dto.importacion.ResultadoImportacionDTO;
import com.boulder.boulder.entities.AvancePerforacion;
import com.boulder.boulder.entities.AvancePersonal;
import com.boulder.boulder.entities.Empleado;
import com.boulder.boulder.entities.Maquina;
import com.boulder.boulder.entities.Plataforma;
import com.boulder.boulder.entities.Sondaje;
import com.boulder.boulder.repositories.AvancePerforacionRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Carga el parte diario de perforación por turno (UC15).
 *
 * <p>
 * Es el parte que hoy se manda por WhatsApp al cierre de turno. La dotación
 * viene en una sola celda, separada por punto y coma, y se abre en tantos
 * renglones de {@code prf_avance_personal} como nombres traiga: el parte de
 * papel la escribe en una línea, pero para cruzar desviación contra perforista
 * hace falta cada uno por separado.
 * </p>
 *
 * <p>
 * Un nombre que no está dado de alta en {@code base.empleado} no rechaza el
 * renglón: se guarda el texto y la relación queda sin resolver. Exigir el alta
 * previa de cada ayudante dejaría afuera la mitad de los partes, y el nombre
 * escrito ya vale más que la ausencia del parte entero.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Component
public class ImportadorPerforacion extends ImportadorBase {

    @Autowired
    private AvancePerforacionRepository repositorio;

    @PersistenceContext
    private EntityManager em;

    @Override
    public String clave() {
        return "perforacion";
    }

    @Override
    public String archivoPlantilla() {
        return "PLANTILLA_parte_perforacion.xlsx";
    }

    @Override
    public String descripcion() {
        return "Parte diario de perforación por turno, con la dotación del turno.";
    }

    @Override
    protected void procesar(Workbook libro, Catalogos catalogos, boolean aplicar,
            ResultadoImportacionDTO resultado) {

        String sqlClaves = "SELECT " + sqlTexto("sondaje_id") + " || '|' || " + sqlTexto("fecha")
                + " || '|' || " + sqlTexto("turno") + " || '|' || " + sqlNum("desde")
                + " FROM operacional.prf_avance";

        hoja(libro, "DATOS", "operacional.prf_avance", sqlClaves, catalogos, aplicar, resultado, f -> {

            UUID sondajeId = f.sondajeReq("sondaje_codigo");
            LocalDate fecha = f.fechaReq("fecha");
            String turno = f.opcion("turno", true, "dia", "noche");
            BigDecimal desde = f.decimalReq("desde");
            BigDecimal hasta = f.decimalReq("hasta");
            BigDecimal avance = f.decimalReq("avance");

            f.referencia(f.texto("sondaje_codigo") + " " + f.texto("fecha") + " " + (turno == null ? "" : turno));

            UUID maquinaId = f.maquina("maquina_codigo");
            UUID plataformaId = f.plataforma("plataforma_codigo");
            BigDecimal retorno = f.decimal("retorno_pct");

            // Las mismas reglas que el formulario: un parte no cuadra distinto
            // según por dónde entre.
            ParteTurno.errores(desde, hasta, avance, retorno,
                    f.decimal("casing_desde"), f.decimal("casing_hasta")).forEach(f::error);

            AvancePerforacion e = new AvancePerforacion();
            e.setSondajeCodigo(f.texto("sondaje_codigo", 30));
            e.setFecha(fecha);
            e.setTurno(turno);
            e.setResponsable(f.texto("responsable", 80));
            e.setDesde(desde);
            e.setHasta(hasta);
            e.setAvance(avance);
            e.setCasingTipo(f.texto("casing_tipo", 15));
            e.setCasingDesde(f.decimal("casing_desde"));
            e.setCasingHasta(f.decimal("casing_hasta"));
            e.setRetornoPct(retorno);
            e.setLinea(f.texto("linea", 10));
            e.setComentarios(f.texto("comentarios"));
            e.setMaquinaCodigo(f.texto("maquina_codigo", 20));
            e.setPlataformaCodigo(f.texto("plataforma_codigo", 20));
            e.setEstado("registrado");

            if (f.hayErrores()) {
                return null;
            }

            e.setSondaje(em.getReference(Sondaje.class, sondajeId));
            if (maquinaId != null) {
                e.setMaquina(em.getReference(Maquina.class, maquinaId));
            }
            if (plataformaId != null) {
                e.setPlataforma(em.getReference(Plataforma.class, plataformaId));
            }

            for (String nombre : f.lista("personal")) {
                AvancePersonal p = new AvancePersonal();
                p.setAvance(e);
                p.setNombre(nombre.length() > 80 ? nombre.substring(0, 80) : nombre);
                p.setCodigoMaquina(e.getMaquinaCodigo());
                UUID empleadoId = f.empleado(nombre);
                if (empleadoId != null) {
                    p.setPerforista(em.getReference(Empleado.class, empleadoId));
                }
                e.getPersonal().add(p);
            }

            return new Registro(clave(sondajeId, fecha, turno, desde), () -> repositorio.saveAndFlush(e));
        });
    }
}
