package com.boulder.boulder.services.importacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Lectura y validación de un renglón de plantilla, celda por celda, sin base. */
class FilaTest {

    private XSSFWorkbook libro;
    private Row row;
    private Map<String, Integer> columnas;
    private Catalogos catalogos;

    @BeforeEach
    void preparar() {
        libro = new XSSFWorkbook();
        row = libro.createSheet("DATOS").createRow(4);
        columnas = new LinkedHashMap<>();
        catalogos = mock(Catalogos.class);
    }

    @AfterEach
    void cerrar() throws Exception {
        libro.close();
    }

    private Fila con(Object... pares) {
        for (int i = 0; i < pares.length; i += 2) {
            int col = columnas.size();
            columnas.put((String) pares[i], col);
            Object v = pares[i + 1];
            if (v instanceof Number n) {
                row.createCell(col).setCellValue(n.doubleValue());
            } else if (v instanceof LocalDate d) {
                CellStyle fecha = libro.createCellStyle();
                fecha.setDataFormat(libro.createDataFormat().getFormat("yyyy-mm-dd"));
                row.createCell(col).setCellValue(d);
                row.getCell(col).setCellStyle(fecha);
            } else if (v != null) {
                row.createCell(col).setCellValue((String) v);
            }
        }
        return new Fila(row, columnas, catalogos);
    }

    @Test
    @DisplayName("El número de fila es el que muestra Excel")
    void numero() {
        assertThat(con().numero()).isEqualTo(5);
    }

    @Test
    @DisplayName("El texto llega recortado y un entero de Excel no arrastra «.0»")
    void texto() {
        Fila f = con("a", "  HQ3 ", "b", 400.0, "c", 12.50);
        assertThat(f.texto("a")).isEqualTo("HQ3");
        assertThat(f.texto("b")).isEqualTo("400");
        assertThat(f.texto("c")).isEqualTo("12.5");
    }

    @Test
    @DisplayName("Un texto más largo que la columna se rechaza con su largo")
    void largo() {
        Fila f = con("codigo", "ABCDEFGHIJK");
        assertThat(f.texto("codigo", 5)).isNull();
        assertThat(f.motivos()).contains("codigo").contains("5").contains("11");
    }

    @Test
    @DisplayName("Un obligatorio vacío o una columna ausente quedan como error")
    void obligatorios() {
        Fila f = con("nombre", "   ");
        assertThat(f.textoReq("nombre", 10)).isNull();
        assertThat(f.decimalReq("desde")).isNull();
        assertThat(f.motivos()).contains("nombre: obligatorio").contains("falta la columna desde");
    }

    @Test
    @DisplayName("La columna que falta se avisa una sola vez")
    void faltaUnaVez() {
        Fila f = con();
        f.decimalReq("desde");
        f.fechaReq("desde");
        assertThat(f.motivos()).isEqualTo("falta la columna desde");
    }

    @Test
    @DisplayName("Las opciones se aceptan sin distinguir mayúsculas y vuelven en su forma canónica")
    void opcion() {
        Fila f = con("turno", "NOCHE", "estado", "raro");
        assertThat(f.opcion("turno", true, "dia", "noche")).isEqualTo("noche");
        assertThat(f.opcion("estado", false, "abierto", "cerrado")).isNull();
        assertThat(f.motivos()).contains("«raro» no es un valor válido");
    }

    @Test
    @DisplayName("Una opción obligatoria vacía lista los valores válidos")
    void opcionObligatoria() {
        Fila f = con("turno", null);
        columnas.put("turno", 0);
        assertThat(f.opcion("turno", true, "dia", "noche")).isNull();
        assertThat(f.motivos()).contains("dia | noche");
    }

    @Test
    @DisplayName("Los decimales aceptan coma y espacios; un texto no numérico es error")
    void decimales() {
        Fila f = con("a", "14,70", "b", "1 250.5", "c", "doce");
        assertThat(f.decimal("a")).isEqualByComparingTo("14.70");
        assertThat(f.doble("b")).isEqualTo(1250.5);
        assertThat(f.decimal("c")).isNull();
        assertThat(f.motivos()).contains("«doce» no es un número");
    }

    @Test
    @DisplayName("Un entero con decimales es error; corto convierte el valor")
    void enteros() {
        Fila f = con("cajas", 12.0, "medicion", 3.0, "mal", 2.5);
        assertThat(f.entero("cajas")).isEqualTo(12);
        assertThat(f.corto("medicion")).isEqualTo((short) 3);
        assertThat(f.entero("mal")).isNull();
        assertThat(f.motivos()).contains("tiene que ser un entero");
    }

    @Test
    @DisplayName("Las fechas se leen de una celda de fecha, en ISO o en d/M/aaaa")
    void fechas() {
        LocalDate dia = LocalDate.of(2026, 9, 7);
        Fila f = con("a", dia, "b", "2026-09-07", "c", "7/9/2026", "d", "ayer");
        assertThat(f.fecha("a")).isEqualTo(dia);
        assertThat(f.fecha("b")).isEqualTo(dia);
        assertThat(f.fecha("c")).isEqualTo(dia);
        assertThat(f.fecha("d")).isNull();
        assertThat(f.motivos()).contains("«ayer» no es una fecha");
        assertThat(f.texto("a")).isEqualTo("2026-09-07");
    }

    @Test
    @DisplayName("Un renglón sin datos está vacío")
    void vacia() {
        assertThat(con("a", "  ", "b", null).vacia()).isTrue();
        assertThat(con("c", "x").vacia()).isFalse();
    }

    @Test
    @DisplayName("El sondaje resuelve contra el catálogo y guarda el código del catálogo")
    void sondaje() {
        UUID id = UUID.randomUUID();
        when(catalogos.sondaje("VERO S 35_26")).thenReturn(id);
        when(catalogos.codigoSondaje(id)).thenReturn("DDH-VERO-S-035-26");
        Fila f = con("sondaje_codigo", "VERO S 35_26");

        assertThat(f.sondajeReq("sondaje_codigo")).isEqualTo(id);
        assertThat(f.codigoSondaje(id, "sondaje_codigo")).isEqualTo("DDH-VERO-S-035-26");
        assertThat(f.hayErrores()).isFalse();
    }

    @Test
    @DisplayName("Un sondaje que no está en el catálogo rechaza el renglón")
    void sondajeInexistente() {
        Fila f = con("sondaje_codigo", "DDH-35");
        assertThat(f.sondajeReq("sondaje_codigo")).isNull();
        assertThat(f.motivos()).contains("«DDH-35» no está en el catálogo");
    }

    @Test
    @DisplayName("VARIOS se acepta sin sondaje y se guarda tal cual")
    void varios() {
        Fila f = con("sondaje_codigo", "varios");
        assertThat(f.sondajeOVarios("sondaje_codigo")).isNull();
        assertThat(f.hayErrores()).isFalse();
        assertThat(f.codigoSondaje(null, "sondaje_codigo")).isEqualTo("varios");
    }

    @Test
    @DisplayName("Máquina, plataforma y diccionario inexistentes son error; el empleado no")
    void catalogos() {
        Fila f = con("maquina", "M-9", "plataforma", "P-9", "lito", "ZZZ", "geologo", "Nadie");
        assertThat(f.maquina("maquina")).isNull();
        assertThat(f.plataforma("plataforma")).isNull();
        assertThat(f.geologia("lito", "litologia", true)).isNull();
        assertThat(f.empleado(f.texto("geologo"))).isNull();
        assertThat(f.motivos()).contains("M-9").contains("P-9").contains("geologia.litologia");
    }

    @Test
    @DisplayName("Una celda con varios nombres se parte por coma, punto y coma o salto de línea")
    void lista() {
        Fila f = con("dotacion", "Ana Ríos; Luis Paz,\nMarta Gil ;");
        assertThat(f.lista("dotacion")).containsExactly("Ana Ríos", "Luis Paz", "Marta Gil");
        assertThat(f.lista("otra")).isEmpty();
    }

    @Test
    @DisplayName("Omitir no es un error y la referencia se conserva")
    void omitir() {
        Fila f = con();
        f.referencia("DDH-VERO-S-035-26 2026-09-07");
        f.omitir("ya cargado");
        assertThat(f.omitida()).isTrue();
        assertThat(f.hayErrores()).isFalse();
        assertThat(f.referencia()).startsWith("DDH-VERO");
    }
}
