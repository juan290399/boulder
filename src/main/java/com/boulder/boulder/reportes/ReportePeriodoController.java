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
 * Reporte consolidado de un periodo. Lo consulta cualquier usuario autenticado.
 */
@RestController
@RequestMapping("/api/reportes/periodo")
public class ReportePeriodoController {

    private static final MediaType XLSX =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final ReportePeriodoService servicio;

    public ReportePeriodoController(ReportePeriodoService servicio) {
        this.servicio = servicio;
    }

    /**
     * @param desde primer día en formato ISO; seis días antes de {@code hasta} si se omite
     * @param hasta último día en formato ISO; hoy si se omite
     */
    @GetMapping
    public ReportePeriodo ver(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return servicio.generar(desde, hasta);
    }

    /** El mismo reporte como libro de Excel. */
    @GetMapping("/excel")
    public ResponseEntity<byte[]> excel(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta)
            throws IOException {
        ReportePeriodo r = servicio.generar(desde, hasta);
        return ResponseEntity.ok()
                .contentType(XLSX)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("reporte_" + r.desde() + "_" + r.hasta() + ".xlsx").build().toString())
                .body(ReportePeriodoExcel.generar(r));
    }
}
