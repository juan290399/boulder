package com.boulder.boulder.reportes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.boulder.boulder.reportes.ReporteDiario.FilaSondaje;

/**
 * Exporta el reporte diario a Excel con Apache POI.
 *
 * <p>Los metros y las cantidades se escriben como números, para que la gerencia
 * pueda sumar y filtrar. Los textos se escriben siempre con
 * {@code setCellValue(String)}, nunca como fórmula: un código de sondaje que
 * empiece con «=» queda como texto y no se ejecuta al abrir el archivo
 * (inyección de fórmulas, CWE-1236).</p>
 */
public final class ReporteDiarioExcel {

    private static final String[] CABECERA = {"Sondaje", "Perforado (m)", "Logueado (m)", "Cajas", "Muestras"};

    private ReporteDiarioExcel() {
    }

    /**
     * @param r el reporte del día
     * @return el libro .xlsx listo para descargar
     * @throws IOException si no se puede escribir el libro
     */
    public static byte[] generar(ReporteDiario r) throws IOException {
        try (XSSFWorkbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Sheet hoja = libro.createSheet(r.fecha().toString());
            CellStyle negrita = estiloNegrita(libro);
            CellStyle metros = libro.createCellStyle();
            metros.setDataFormat(libro.createDataFormat().getFormat("#,##0.00"));

            texto(hoja.createRow(0), 0, "Reporte diario consolidado del " + r.fecha(), negrita);

            Row cab = hoja.createRow(2);
            for (int i = 0; i < CABECERA.length; i++) {
                texto(cab, i, CABECERA[i], negrita);
            }

            int fila = 3;
            for (FilaSondaje s : r.sondajes()) {
                Row row = hoja.createRow(fila++);
                texto(row, 0, s.codigo(), null);
                numero(row, 1, s.perforado(), metros);
                numero(row, 2, s.logueado(), metros);
                row.createCell(3).setCellValue(s.cajas());
                row.createCell(4).setCellValue(s.muestras());
            }

            Row total = hoja.createRow(fila);
            texto(total, 0, "Total", negrita);
            numero(total, 1, r.totalPerforado(), metros);
            numero(total, 2, r.totalLogueado(), metros);
            total.createCell(3).setCellValue(r.totalCajas());
            total.createCell(4).setCellValue(r.totalMuestras());

            for (int i = 0; i < CABECERA.length; i++) {
                hoja.autoSizeColumn(i);
            }
            libro.write(salida);
            return salida.toByteArray();
        }
    }

    private static void texto(Row fila, int col, String valor, CellStyle estilo) {
        Cell c = fila.createCell(col);
        c.setCellValue(valor);
        if (estilo != null) {
            c.setCellStyle(estilo);
        }
    }

    private static void numero(Row fila, int col, BigDecimal valor, CellStyle estilo) {
        Cell c = fila.createCell(col);
        c.setCellValue(valor.doubleValue());
        c.setCellStyle(estilo);
    }

    private static CellStyle estiloNegrita(XSSFWorkbook libro) {
        Font f = libro.createFont();
        f.setBold(true);
        CellStyle s = libro.createCellStyle();
        s.setFont(f);
        s.setBorderBottom(BorderStyle.THIN);
        return s;
    }
}
