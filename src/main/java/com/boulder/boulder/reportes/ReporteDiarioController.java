package com.boulder.boulder.reportes;

import java.io.IOException;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Reporte diario consolidado (pantalla P11). Lo consulta cualquier usuario
 * autenticado, incluida la gerencia con el rol VISITANTE.
 */
@RestController
@RequestMapping("/api/reportes/diario")
public class ReporteDiarioController {

    private static final MediaType XLSX =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final ReporteDiarioService servicio;

    public ReporteDiarioController(ReporteDiarioService servicio) {
        this.servicio = servicio;
    }

    /** @param fecha día en formato ISO ({@code 2026-09-07}); hoy si se omite */
    @GetMapping
    public ReporteDiario ver(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return servicio.generar(fecha);
    }

    /** El mismo reporte como libro de Excel para descargar. */
    @GetMapping("/excel")
    public ResponseEntity<byte[]> excel(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha)
            throws IOException {
        ReporteDiario r = servicio.generar(fecha);
        return ResponseEntity.ok()
                .contentType(XLSX)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("reporte_diario_" + r.fecha() + ".xlsx").build().toString())
                .body(ReporteDiarioExcel.generar(r));
    }
}
