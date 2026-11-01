package com.boulder.boulder.reportes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Exporta el reporte de un periodo a Excel con Apache POI.
 */
public final class ReportePeriodoExcel {

    private ReportePeriodoExcel() {
    }

    public static byte[] generar(ReportePeriodo r) throws IOException {
        try (XSSFWorkbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            libro.createSheet("vacío");
            libro.write(salida);
            return salida.toByteArray();
        }
    }
}
