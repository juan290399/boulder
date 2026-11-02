package com.boulder.boulder.reportes;

import java.io.IOException;
import java.util.UUID;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Hoja de registro de quicklog de un sondaje, en Excel. Cualquier sesión la descarga. */
@RestController
@RequestMapping("/api/reportes/quicklog")
public class HojaQuicklogController {

    private static final MediaType XLSX =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final HojaQuicklogService servicio;

    public HojaQuicklogController(HojaQuicklogService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/excel")
    public ResponseEntity<byte[]> excel(@RequestParam UUID sondajeId) throws IOException {
        HojaQuicklog h = servicio.generar(sondajeId);
        return ResponseEntity.ok()
                .contentType(XLSX)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("quicklog_" + h.cabecera().sondaje() + ".xlsx").build().toString())
                .body(HojaQuicklogExcel.generar(h));
    }
}
