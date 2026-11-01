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
import com.boulder.boulder.reportes.ReportePeriodo.FilaDia;

class ReportePeriodoExcelTest {

    private static final LocalDate DESDE = LocalDate.of(2026, 9, 1);
    private static final LocalDate HASTA = LocalDate.of(2026, 9, 2);

    private static ReportePeriodo reporte(String codigo) {
        return ReportePeriodo.de(DESDE, HASTA,
                List.of(new FilaSondaje(codigo, new BigDecimal("40.00"), new BigDecimal("30.00"), 10, 12),
                        new FilaSondaje("DDH-VERO-S-038-26", new BigDecimal("15.50"), BigDecimal.ZERO, 4, 0)),
                List.of(new FilaDia(DESDE, new BigDecimal("55.50"), new BigDecimal("30.00"), 14, 12)));
    }

    private static Workbook abrir(byte[] bytes) throws Exception {
        return new XSSFWorkbook(new ByteArrayInputStream(bytes));
    }

    @Test
    @DisplayName("Hoja por sondaje con cabecera, filas ordenadas y totales")
    void hojaSondajes() throws Exception {
        try (Workbook libro = abrir(ReportePeriodoExcel.generar(reporte("DDH-VERO-S-040-26")))) {
            Sheet hoja = libro.getSheet("Por sondaje");
            assertThat(hoja.getRow(0).getCell(0).getStringCellValue()).contains("2026-09-01").contains("2026-09-02");
            assertThat(hoja.getRow(2).getCell(0).getStringCellValue()).isEqualTo("Sondaje");
            assertThat(hoja.getRow(3).getCell(0).getStringCellValue()).isEqualTo("DDH-VERO-S-038-26");
            assertThat(hoja.getRow(5).getCell(0).getStringCellValue()).isEqualTo("Total");
            assertThat(hoja.getRow(5).getCell(1).getNumericCellValue()).isEqualTo(55.50);
            assertThat(hoja.getRow(3).getCell(1).getCellType()).isEqualTo(CellType.NUMERIC);
        }
    }

    @Test
    @DisplayName("Hoja por día con todos los días del periodo como fechas")
    void hojaDias() throws Exception {
        try (Workbook libro = abrir(ReportePeriodoExcel.generar(reporte("DDH-VERO-S-040-26")))) {
            Sheet hoja = libro.getSheet("Por día");
            assertThat(hoja.getRow(2).getCell(0).getStringCellValue()).isEqualTo("Fecha");
            assertThat(hoja.getRow(3).getCell(0).getLocalDateTimeCellValue().toLocalDate()).isEqualTo(DESDE);
            assertThat(hoja.getRow(4).getCell(0).getLocalDateTimeCellValue().toLocalDate()).isEqualTo(HASTA);
            assertThat(hoja.getRow(4).getCell(1).getNumericCellValue()).isZero();
        }
    }

    @Test
    @DisplayName("Un código que empieza con = queda como texto")
    void sinFormulas() throws Exception {
        try (Workbook libro = abrir(ReportePeriodoExcel.generar(reporte("=1+1")))) {
            Sheet hoja = libro.getSheet("Por sondaje");
            assertThat(hoja.getRow(3).getCell(0).getCellType()).isEqualTo(CellType.STRING);
        }
    }
}
