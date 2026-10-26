package com.boulder.boulder.services.importacion;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import com.boulder.boulder.dto.importacion.ResultadoHojaDTO;
import com.boulder.boulder.dto.importacion.ResultadoImportacionDTO;
import com.boulder.boulder.services.ImportacionService;

/**
 * Pruebas de integración del importador con las cinco plantillas oficiales de
 * Excel (copiadas de boulder-shared/db/plantillas a src/test/resources) contra la
 * base PostgreSQL real. Cada prueba corre en una transacción que se deshace.
 */
@SpringBootTest
@Transactional
class ImportacionIT {

    @Autowired
    private ImportacionService importacion;

    private static byte[] leer(String archivo) throws Exception {
        try (InputStream in = ImportacionIT.class.getResourceAsStream("/plantillas/" + archivo)) {
            return in.readAllBytes();
        }
    }

    private ResultadoImportacionDTO importar(String plantilla, byte[] libro, boolean aplicar) {
        MockMultipartFile archivo = new MockMultipartFile("archivo", plantilla + ".xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", libro);
        return importacion.importar(plantilla, archivo, aplicar);
    }

    private static String resumen(ResultadoImportacionDTO r) {
        StringBuilder sb = new StringBuilder();
        for (ResultadoHojaDTO h : r.getHojas()) {
            sb.append(h.getHoja()).append(": leídas=").append(h.getLeidas())
              .append(" aceptadas=").append(h.getAceptadas()).append(" omitidas=").append(h.getOmitidas())
              .append(" rechazadas=").append(h.getRechazadas()).append(' ');
            h.getErrores().forEach(e -> sb.append("[fila ").append(e.getFila()).append(": ").append(e.getMotivo()).append("] "));
        }
        return sb.toString();
    }

    @ParameterizedTest(name = "{0}: la fila de ejemplo pasa la validación")
    @CsvSource({
            "perforacion, PLANTILLA_parte_perforacion.xlsx",
            "almacen,     PLANTILLA_reporte_almacen.xlsx",
            "logueo,      PLANTILLA_avance_logueo.xlsx",
            "desvio,      PLANTILLA_desvio.xlsx",
            "quicklog,    PLANTILLA_quicklog.xlsx",
    })
    @DisplayName("Simular la carga de cada plantilla oficial no rechaza su ejemplo ni escribe")
    void simulacion(String plantilla, String archivo) throws Exception {
        ResultadoImportacionDTO r = importar(plantilla, leer(archivo), false);

        assertThat(r.isAplicado()).as("una simulación nunca queda aplicada").isFalse();
        assertThat(r.getTotalLeidas()).as(resumen(r)).isPositive();
        assertThat(r.getTotalRechazadas()).as(resumen(r)).isZero();
    }

    /**
     * Copia la plantilla de quicklog cambiando celdas de la fila de ejemplo (la
     * fila 2 de Excel en la hoja DATOS), buscadas por el nombre de su cabecera.
     */
    private static byte[] quicklogCon(java.util.Map<String, Object> cambios) throws Exception {
        try (Workbook wb = new XSSFWorkbook(ImportacionIT.class.getResourceAsStream("/plantillas/PLANTILLA_quicklog.xlsx"));
             ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Sheet hoja = wb.getSheet("DATOS");
            Row cabecera = hoja.getRow(0);
            Row ejemplo = hoja.getRow(1);
            cambios.forEach((columna, valor) -> {
                Cell celda = null;
                for (Cell c : cabecera) {
                    if (columna.equals(c.getStringCellValue())) {
                        celda = ejemplo.getCell(c.getColumnIndex(), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    }
                }
                assertThat(celda).as("columna " + columna).isNotNull();
                if (valor instanceof Number n) {
                    celda.setCellValue(n.doubleValue());
                } else {
                    celda.setCellValue(String.valueOf(valor));
                }
            });
            wb.write(salida);
            return salida.toByteArray();
        }
    }

    @Test
    @DisplayName("Un código geológico que no existe se rechaza con la fila de Excel y el motivo")
    void codigoInexistente() throws Exception {
        ResultadoImportacionDTO r = importar("quicklog", quicklogCon(java.util.Map.of("lito", "ZZZZ")), false);

        assertThat(r.getTotalRechazadas()).as(resumen(r)).isEqualTo(1);
        var error = r.getHojas().stream().flatMap(h -> h.getErrores().stream()).findFirst().orElseThrow();
        assertThat(error.getFila()).as("número de fila tal como se ve en Excel").isEqualTo(2);
        assertThat(error.getMotivo()).containsIgnoringCase("ZZZZ");
    }

    @Test
    @DisplayName("La fila de ejemplo ya está en la base: se omite, no se duplica")
    void ejemploYaCargado() throws Exception {
        ResultadoImportacionDTO r = importar("quicklog", leer("PLANTILLA_quicklog.xlsx"), false);
        assertThat(r.getTotalOmitidas()).as(resumen(r)).isEqualTo(1);
        assertThat(r.getTotalAceptadas()).as(resumen(r)).isZero();
    }

    @Test
    @DisplayName("Cargar dos veces el mismo archivo no duplica: la segunda vez se omite")
    void sinDuplicados() throws Exception {
        // Un tramo que no existe: muy por debajo del fondo del sondaje de ejemplo.
        byte[] libro = quicklogCon(java.util.Map.of("desde", 9990.0, "hasta", 9991.5, "longitud", 1.5));

        ResultadoImportacionDTO primera = importar("quicklog", libro, true);
        ResultadoImportacionDTO segunda = importar("quicklog", libro, false);

        assertThat(primera.isAplicado()).isTrue();
        assertThat(primera.getTotalAceptadas()).as(resumen(primera)).isEqualTo(1);
        assertThat(segunda.getTotalAceptadas()).as(resumen(segunda)).isZero();
        assertThat(segunda.getTotalOmitidas()).as(resumen(segunda)).isEqualTo(1);
    }
}
