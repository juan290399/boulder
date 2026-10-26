package com.boulder.boulder.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.boulder.boulder.dto.importacion.PlantillaDTO;
import com.boulder.boulder.dto.importacion.ResultadoImportacionDTO;
import com.boulder.boulder.eventos.CambioDatos;
import com.boulder.boulder.services.importacion.Catalogos;
import com.boulder.boulder.services.importacion.ImportacionAbortadaException;
import com.boulder.boulder.services.importacion.ImportadorPlantilla;

/**
 * Despacha el libro al importador que corresponde y decide si lo que se hizo
 * se guarda.
 *
 * <p>
 * La simulación no es una comodidad, es el modo normal de trabajo. Quien llena
 * una plantilla de dos mil renglones necesita ver los cuarenta que están mal
 * antes de escribir nada, corregirlos y recién entonces aplicar. Por eso
 * {@code aplicar} viene en falso si no se pide lo contrario: el camino fácil
 * tiene que ser el que no toca la base.
 * </p>
 *
 * <p>
 * Cuando sí se aplica, todo va en una transacción. Los renglones inválidos ya
 * quedaron afuera antes de llegar acá; si a pesar de eso la base rechaza uno,
 * no se guarda nada. Media plantilla cargada es peor que ninguna, porque
 * obliga a averiguar por dónde iba para poder retomar.
 * </p>
 *
 * @author jvelazco
 * @version 1.0
 * @since 1.0
 */
@Service
public class ImportacionServiceImpl implements ImportacionService {

    private static final Logger log = LoggerFactory.getLogger(ImportacionServiceImpl.class);

    private final Map<String, ImportadorPlantilla> importadores;
    private final JdbcTemplate jdbc;
    private final TransactionTemplate tx;
    private final ApplicationEventPublisher eventos;

    @Autowired
    public ImportacionServiceImpl(List<ImportadorPlantilla> importadores, JdbcTemplate jdbc,
            TransactionTemplate tx, ApplicationEventPublisher eventos) {
        this.importadores = importadores.stream()
                .collect(Collectors.toMap(ImportadorPlantilla::clave, Function.identity()));
        this.jdbc = jdbc;
        this.tx = tx;
        this.eventos = eventos;
    }

    @Override
    public List<PlantillaDTO> plantillas() {
        return importadores.values().stream()
                .sorted(Comparator.comparing(ImportadorPlantilla::clave))
                .map(i -> new PlantillaDTO(i.clave(), i.archivoPlantilla(), i.descripcion()))
                .toList();
    }

    @Override
    public ResultadoImportacionDTO importar(String plantilla, MultipartFile archivo, boolean aplicar) {
        ImportadorPlantilla importador = importadores.get(plantilla);
        if (importador == null) {
            throw new IllegalArgumentException("No hay importador para la plantilla «" + plantilla
                    + "». Disponibles: " + String.join(", ", importadores.keySet()));
        }
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("No llegó ningún archivo.");
        }
        String nombre = archivo.getOriginalFilename();
        if (nombre != null && !nombre.toLowerCase().endsWith(".xlsx")) {
            throw new IllegalArgumentException(
                    "El archivo tiene que ser .xlsx. El formato viejo .xls no admite la validación "
                            + "de datos que llevan las plantillas.");
        }

        log.info("Importando plantilla {} desde {} (aplicar={})", plantilla, nombre, aplicar);

        try (InputStream in = archivo.getInputStream(); Workbook libro = new XSSFWorkbook(in)) {
            Catalogos catalogos = new Catalogos(jdbc);
            ResultadoImportacionDTO resultado = aplicar
                    ? tx.execute(estado -> importador.importar(libro, catalogos, true))
                    : importador.importar(libro, catalogos, false);
            resultado.setArchivo(nombre);
            if (aplicar && resultado.getTotalAceptadas() > 0) {
                // Sin lista de sondajes: una carga puede tocar decenas, y las
                // pantallas abiertas simplemente recargan lo que muestran.
                eventos.publishEvent(CambioDatos.de(plantilla, java.util.Set.of(), "importacion",
                        resultado.getTotalAceptadas()));
            }
            log.info("Plantilla {}: {} leídas, {} aceptadas, {} omitidas, {} rechazadas",
                    plantilla, resultado.getTotalLeidas(), resultado.getTotalAceptadas(),
                    resultado.getTotalOmitidas(), resultado.getTotalRechazadas());
            return resultado;
        } catch (ImportacionAbortadaException e) {
            log.error("Importación abortada", e);
            throw e;
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "No se pudo leer el archivo: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            // POI avisa con excepciones no comprobadas cuando lo que llega no
            // es un .xlsx: un .xls renombrado, un csv, o un libro dañado.
            throw new IllegalArgumentException(
                    "No se pudo abrir el archivo como libro de Excel: " + e.getMessage(), e);
        }
    }
}
