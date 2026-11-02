package com.boulder.boulder.reportes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/** Exporta la hoja de registro de quicklog a Excel. */
public final class HojaQuicklogExcel {

    private HojaQuicklogExcel() {
    }

    public static byte[] generar(HojaQuicklog h) throws IOException {
        try (XSSFWorkbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            libro.createSheet("vacío");
            libro.write(salida);
            return salida.toByteArray();
        }
    }
}
