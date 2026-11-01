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
import com.boulder.boulder.reportes.ReportePeriodo.FilaDia;

/**
 * Exporta el reporte de un periodo a Excel con Apache POI: una hoja por sondaje
 * y otra por día. Números como números, fechas como fechas y textos siempre
 * como texto, nunca como fórmula (CWE-1236).
 */
public final class ReportePeriodoExcel {

    private static final String[] CAB_SONDAJE = {"Sondaje", "Perforado (m)", "Logueado (m)", "Cajas", "Muestras"};
    private static final String[] CAB_DIA = {"Fecha", "Perforado (m)", "Logueado (m)", "Cajas", "Muestras"};

    private ReportePeriodoExcel() {
    }

    /**
     * @param r el reporte del periodo
     * @return el libro .xlsx
     * @throws IOException si no se puede escribir el libro
     */
    public static byte[] generar(ReportePeriodo r) throws IOException {
        try (XSSFWorkbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            CellStyle negrita = estiloNegrita(libro);
            CellStyle metros = libro.createCellStyle();
            metros.setDataFormat(libro.createDataFormat().getFormat("#,##0.00"));
            CellStyle fecha = libro.createCellStyle();
            fecha.setDataFormat(libro.createDataFormat().getFormat("yyyy-mm-dd"));
            String titulo = "Reporte consolidado del " + r.desde() + " al " + r.hasta();

            Sheet porSondaje = libro.createSheet("Por sondaje");
            texto(porSondaje.createRow(0), 0, titulo, negrita);
            cabecera(porSondaje.createRow(2), CAB_SONDAJE, negrita);
            int fila = 3;
            for (FilaSondaje s : r.sondajes()) {
                Row row = porSondaje.createRow(fila++);
                texto(row, 0, s.codigo(), null);
                valores(row, s.perforado(), s.logueado(), s.cajas(), s.muestras(), metros);
            }
            Row total = porSondaje.createRow(fila);
            texto(total, 0, "Total", negrita);
            valores(total, r.totalPerforado(), r.totalLogueado(), r.totalCajas(), r.totalMuestras(), metros);
            ajustar(porSondaje, CAB_SONDAJE.length);

            Sheet porDia = libro.createSheet("Por día");
            texto(porDia.createRow(0), 0, titulo, negrita);
            cabecera(porDia.createRow(2), CAB_DIA, negrita);
            fila = 3;
            for (FilaDia d : r.dias()) {
                Row row = porDia.createRow(fila++);
                Cell c = row.createCell(0);
                c.setCellValue(d.fecha());
                c.setCellStyle(fecha);
                valores(row, d.perforado(), d.logueado(), d.cajas(), d.muestras(), metros);
            }
            ajustar(porDia, CAB_DIA.length);

            libro.write(salida);
            return salida.toByteArray();
        }
    }

    private static void cabecera(Row row, String[] titulos, CellStyle estilo) {
        for (int i = 0; i < titulos.length; i++) {
            texto(row, i, titulos[i], estilo);
        }
    }

    private static void valores(Row row, BigDecimal perforado, BigDecimal logueado, int cajas, int muestras,
                                CellStyle metros) {
        numero(row, 1, perforado, metros);
        numero(row, 2, logueado, metros);
        row.createCell(3).setCellValue(cajas);
        row.createCell(4).setCellValue(muestras);
    }

    private static void ajustar(Sheet hoja, int columnas) {
        for (int i = 0; i < columnas; i++) {
            hoja.autoSizeColumn(i);
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
