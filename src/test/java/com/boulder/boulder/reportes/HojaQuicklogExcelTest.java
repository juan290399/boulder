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

import com.boulder.boulder.reportes.HojaQuicklog.Cabecera;
import com.boulder.boulder.reportes.HojaQuicklog.Tramo;

class HojaQuicklogExcelTest {

    private static final LocalDate DIA = LocalDate.of(2026, 7, 4);

    private static HojaQuicklog hoja(String comentario) {
        Cabecera c = new Cabecera("DDH-VERO-S-035-26", "VERONICA", "C8C", 77.5, -62.5,
                8306693.622, 617299.988, 3889.916, 550.0);
        return HojaQuicklog.de(c, List.of(
                new Tramo(DIA, new BigDecimal("40.10"), new BigDecimal("54.30"), new BigDecimal("14.20"), "GRD",
                        "CLTZ-ARGI", "CLT-CAO", "PYT-CPY", new BigDecimal("0.5"), new BigDecimal("0.3"), null,
                        "VLLA", "SFPR", comentario, "Jimmy Velazco"),
                new Tramo(DIA.minusDays(2), BigDecimal.ZERO, new BigDecimal("0.80"), new BigDecimal("0.80"), "GRD",
                        "ARGI", "CAO", null, null, null, null, null, null, null, "Yanely Roque")));
    }

    private static Workbook abrir(byte[] b) throws Exception {
        return new XSSFWorkbook(new ByteArrayInputStream(b));
    }

    @Test
    @DisplayName("Los tramos salen por profundidad y la cabecera lista a los geólogos")
    void orden() {
        HojaQuicklog h = hoja("x");
        assertThat(h.tramos().getFirst().desde()).isEqualByComparingTo("0");
        assertThat(h.geologos()).containsExactly("Jimmy Velazco", "Yanely Roque");
        assertThat(h.metrosLogueados()).isEqualByComparingTo("15.00");
    }

    @Test
    @DisplayName("La cabecera del sondaje va arriba, como en la hoja de registro de campo")
    void cabecera() throws Exception {
        try (Workbook libro = abrir(HojaQuicklogExcel.generar(hoja("x")))) {
            Sheet s = libro.getSheet("Hoja de registro");
            assertThat(s.getRow(0).getCell(0).getStringCellValue()).contains("QUICKLOG");
            assertThat(s.getRow(1).getCell(1).getStringCellValue()).isEqualTo("DDH-VERO-S-035-26");
            assertThat(s.getRow(1).getCell(3).getStringCellValue()).isEqualTo("VERONICA");
            assertThat(s.getRow(2).getCell(3).getNumericCellValue()).isEqualTo(77.5);
            assertThat(s.getRow(2).getCell(5).getNumericCellValue()).isEqualTo(-62.5);
            assertThat(s.getRow(4).getCell(3).getNumericCellValue()).isEqualTo(550.0);
        }
    }

    @Test
    @DisplayName("Una fila por tramo con números como números y la fecha como fecha")
    void tramos() throws Exception {
        try (Workbook libro = abrir(HojaQuicklogExcel.generar(hoja("Granodiorita cloritizada")))) {
            Sheet s = libro.getSheet("Hoja de registro");
            assertThat(s.getRow(6).getCell(0).getStringCellValue()).isEqualTo("Fecha");
            assertThat(s.getRow(7).getCell(0).getLocalDateTimeCellValue().toLocalDate()).isEqualTo(DIA.minusDays(2));
            assertThat(s.getRow(8).getCell(1).getNumericCellValue()).isEqualTo(40.10);
            assertThat(s.getRow(8).getCell(3).getCellType()).isEqualTo(CellType.NUMERIC);
            assertThat(s.getRow(8).getCell(4).getStringCellValue()).isEqualTo("GRD");
            assertThat(s.getRow(8).getCell(5).getStringCellValue()).isEqualTo("CLTZ-ARGI");
            assertThat(s.getRow(8).getCell(13).getStringCellValue()).isEqualTo("Granodiorita cloritizada");
            assertThat(s.getRow(8).getCell(14).getStringCellValue()).isEqualTo("Jimmy Velazco");
        }
    }

    @Test
    @DisplayName("Un comentario que empieza con = queda como texto")
    void sinFormulas() throws Exception {
        try (Workbook libro = abrir(HojaQuicklogExcel.generar(hoja("=HYPERLINK(\"x\")")))) {
            assertThat(libro.getSheet("Hoja de registro").getRow(8).getCell(13).getCellType()).isEqualTo(CellType.STRING);
        }
    }
}
