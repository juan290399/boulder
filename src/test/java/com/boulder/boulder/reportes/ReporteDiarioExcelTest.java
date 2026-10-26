package com.boulder.boulder.reportes;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.boulder.boulder.reportes.ReporteDiario.FilaSondaje;
import com.boulder.boulder.reportes.ReporteDiario.PuntoSerie;

/**
 * El Excel del reporte diario se vuelve a abrir con Apache POI y se revisa celda
 * por celda: lo que importa es que la gerencia pueda sumar y filtrar.
 */
class ReporteDiarioExcelTest {

    private static final LocalDate DIA = LocalDate.of(2026, 9, 7);

    private static ReporteDiario reporte(String codigo) {
        List<FilaSondaje> filas = List.of(
                new FilaSondaje(codigo, new BigDecimal("20.20"), new BigDecimal("35.00"), 6, 12),
                new FilaSondaje("DDH-VERO-S-038-26", new BigDecimal("28.30"), BigDecimal.ZERO, 9, 0));
        return ReporteDiario.de(DIA, filas, List.of(new PuntoSerie(DIA, new BigDecimal("48.50"))));
    }

    private static Workbook abrir(byte[] bytes) throws Exception {
        return new XSSFWorkbook(new ByteArrayInputStream(bytes));
    }

    @Test
    @DisplayName("Una hoja por día, con cabecera, una fila por sondaje y la fila de totales")
    void estructura() throws Exception {
        try (Workbook libro = abrir(ReporteDiarioExcel.generar(reporte("DDH-VERO-S-040-26")))) {
            Sheet hoja = libro.getSheet("2026-09-07");
            assertThat(hoja).isNotNull();
            assertThat(hoja.getRow(2).getCell(0).getStringCellValue()).isEqualTo("Sondaje");
            assertThat(hoja.getRow(3).getCell(0).getStringCellValue()).isEqualTo("DDH-VERO-S-040-26");
            assertThat(hoja.getRow(5).getCell(0).getStringCellValue()).isEqualTo("Total");
            assertThat(hoja.getRow(5).getCell(1).getNumericCellValue()).isEqualTo(48.50);
        }
    }

    @Test
    @DisplayName("Los metros y las cantidades son números, no texto: se pueden sumar en Excel")
    void numericos() throws Exception {
        try (Workbook libro = abrir(ReporteDiarioExcel.generar(reporte("DDH-VERO-S-040-26")))) {
            Sheet hoja = libro.getSheetAt(0);
            for (int col = 1; col <= 4; col++) {
                assertThat(hoja.getRow(3).getCell(col).getCellType()).isEqualTo(CellType.NUMERIC);
            }
        }
    }

    @Test
    @DisplayName("Un código que empieza con «=» se escribe como texto, nunca como fórmula")
    void sinInyeccionDeFormulas() throws Exception {
        try (Workbook libro = abrir(ReporteDiarioExcel.generar(reporte("=HYPERLINK(\"http://x\")")))) {
            Sheet hoja = libro.getSheetAt(0);
            assertThat(hoja.getRow(3).getCell(0).getCellType()).isEqualTo(CellType.STRING);
        }
    }
}
