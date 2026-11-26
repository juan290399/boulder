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

import com.boulder.boulder.reportes.HojaQuicklog.Cabecera;
import com.boulder.boulder.reportes.HojaQuicklog.Tramo;

/**
 * Exporta la hoja de registro de quicklog a Excel con Apache POI, con la
 * disposición de la hoja de campo: cabecera del sondaje y un renglón por tramo.
 * Textos siempre como texto, nunca como fórmula (CWE-1236).
 */
public final class HojaQuicklogExcel {

    private static final String[] COLUMNAS = {"Fecha", "Desde", "Hasta", "Avance", "Lito", "Alteración",
            "Ensamble alt.", "Mineralización", "Py %", "Cu %", "Mo %", "Estilo", "Zona mineral", "Comentario",
            "Responsable"};
    private static final int FILA_COLUMNAS = 6;

    private HojaQuicklogExcel() {
    }

    /**
     * @param h la hoja del sondaje
     * @return el libro .xlsx
     * @throws IOException si no se puede escribir el libro
     */
    public static byte[] generar(HojaQuicklog h) throws IOException {
        try (XSSFWorkbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            CellStyle negrita = estiloNegrita(libro);
            CellStyle metros = libro.createCellStyle();
            metros.setDataFormat(libro.createDataFormat().getFormat("#,##0.00"));
            CellStyle fecha = libro.createCellStyle();
            fecha.setDataFormat(libro.createDataFormat().getFormat("yyyy-mm-dd"));

            Sheet s = libro.createSheet("Hoja de registro");
            Cabecera c = h.cabecera();
            texto(s.createRow(0), 0, "HOJA DE REGISTRO QUICKLOG", negrita);

            Row r1 = s.createRow(1);
            texto(r1, 0, "Taladro:", negrita);
            texto(r1, 1, c.sondaje(), null);
            texto(r1, 2, "Ubicación:", negrita);
            texto(r1, 3, c.ubicacion(), null);

            Row r2 = s.createRow(2);
            texto(r2, 0, "Máquina:", negrita);
            texto(r2, 1, c.maquina(), null);
            texto(r2, 2, "Azimut:", negrita);
            numero(r2, 3, c.azimut(), null);
            texto(r2, 4, "Dip:", negrita);
            numero(r2, 5, c.dip(), null);

            Row r3 = s.createRow(3);
            texto(r3, 0, "Norte:", negrita);
            numero(r3, 1, c.norte(), metros);
            texto(r3, 2, "Este:", negrita);
            numero(r3, 3, c.este(), metros);
            texto(r3, 4, "Cota:", negrita);
            numero(r3, 5, c.cota(), metros);

            Row r4 = s.createRow(4);
            texto(r4, 0, "Geólogo:", negrita);
            texto(r4, 1, String.join(", ", h.geologos()), null);
            texto(r4, 2, "Prof. programada:", negrita);
            numero(r4, 3, c.profundidadProgramada(), metros);
            texto(r4, 4, "Logueado (m):", negrita);
            numero(r4, 5, h.metrosLogueados().doubleValue(), metros);

            Row cab = s.createRow(FILA_COLUMNAS);
            for (int i = 0; i < COLUMNAS.length; i++) {
                texto(cab, i, COLUMNAS[i], negrita);
            }
            int fila = FILA_COLUMNAS + 1;
            for (Tramo t : h.tramos()) {
                Row row = s.createRow(fila++);
                if (t.fecha() != null) {
                    Cell f = row.createCell(0);
                    f.setCellValue(t.fecha());
                    f.setCellStyle(fecha);
                }
                decimal(row, 1, t.desde(), metros);
                decimal(row, 2, t.hasta(), metros);
                decimal(row, 3, t.longitud(), metros);
                texto(row, 4, t.lito(), null);
                texto(row, 5, t.alteracion(), null);
                texto(row, 6, t.ensambleAlteracion(), null);
                texto(row, 7, t.mineralizacion(), null);
                decimal(row, 8, t.pcPy(), null);
                decimal(row, 9, t.pcCu(), null);
                decimal(row, 10, t.pcMo(), null);
                texto(row, 11, t.estilo(), null);
                texto(row, 12, t.zona(), null);
                texto(row, 13, t.comentario(), null);
                texto(row, 14, t.geologo(), null);
            }
            for (int i = 0; i < COLUMNAS.length - 2; i++) {
                s.autoSizeColumn(i);
            }
            s.setColumnWidth(13, 60 * 256);
            s.setColumnWidth(14, 22 * 256);
            s.createFreezePane(0, FILA_COLUMNAS + 1);

            libro.write(salida);
            return salida.toByteArray();
        }
    }

    private static void texto(Row fila, int col, String valor, CellStyle estilo) {
        if (valor == null) {
            return;
        }
        Cell c = fila.createCell(col);
        c.setCellValue(valor);
        if (estilo != null) {
            c.setCellStyle(estilo);
        }
    }

    private static void numero(Row fila, int col, Double valor, CellStyle estilo) {
        if (valor == null) {
            return;
        }
        Cell c = fila.createCell(col);
        c.setCellValue(valor);
        if (estilo != null) {
            c.setCellStyle(estilo);
        }
    }

    private static void decimal(Row fila, int col, BigDecimal valor, CellStyle estilo) {
        numero(fila, col, valor == null ? null : valor.doubleValue(), estilo);
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
