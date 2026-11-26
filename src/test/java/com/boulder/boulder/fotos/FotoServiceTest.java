package com.boulder.boulder.fotos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import com.boulder.boulder.entities.Foto;
import com.boulder.boulder.entities.Sondaje;
import com.boulder.boulder.eventos.CambioDatos;
import com.boulder.boulder.fotos.FotoService.Metadatos;
import com.boulder.boulder.repositories.FotoRepository;
import com.boulder.boulder.repositories.SondajeRepository;

import jakarta.persistence.EntityNotFoundException;

class FotoServiceTest {

    private static final UUID SONDAJE = UUID.randomUUID();

    private FotoRepository fotos;
    private AlmacenFotos almacen;
    private ApplicationEventPublisher eventos;
    private FotoService servicio;

    @BeforeEach
    void preparar() {
        fotos = mock(FotoRepository.class);
        SondajeRepository sondajes = mock(SondajeRepository.class);
        almacen = mock(AlmacenFotos.class);
        eventos = mock(ApplicationEventPublisher.class);
        Sondaje s = new Sondaje();
        s.setId(SONDAJE);
        s.setCodigo("DDH-VERO-S-035-26");
        when(sondajes.findById(SONDAJE)).thenReturn(Optional.of(s));
        when(fotos.findById(any())).thenReturn(Optional.empty());
        when(fotos.save(any(Foto.class))).thenAnswer(i -> i.getArgument(0));
        servicio = new FotoService(fotos, sondajes, almacen, eventos);
    }

    private static byte[] png(int ancho, int alto) throws Exception {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB), "png", salida);
        return salida.toByteArray();
    }

    private static Metadatos metadatos(String condicion, String desde, String hasta, Integer cajaDesde, Integer cajaHasta) {
        return new Metadatos(SONDAJE, cajaDesde, cajaHasta, desde == null ? null : new BigDecimal(desde),
                hasta == null ? null : new BigDecimal(hasta), condicion, OffsetDateTime.now(), null);
    }

    @Test
    @DisplayName("Guarda el original y la miniatura en la carpeta del sondaje, con sus datos")
    void subir() throws Exception {
        UUID id = UUID.randomUUID();
        Foto f = servicio.subir(id, metadatos("seco", "247.25", "283.95", 133, 142), png(1200, 600),
                "C:\\fakepath\\SONDAJE_C133-142.png", "geologo", "tablet-1");

        String clave = "DDH-VERO-S-035-26/" + id + ".png";
        verify(almacen).guardar(eq(clave), any());
        verify(almacen).guardar(eq("DDH-VERO-S-035-26/" + id + "_min.jpg"), any());
        assertThat(f.getTipoMime()).isEqualTo("image/png");
        assertThat(f.getAncho()).isEqualTo(1200);
        assertThat(f.getAlto()).isEqualTo(600);
        assertThat(f.getSha256()).hasSize(64);
        assertThat(f.getArchivoOriginal()).isEqualTo("SONDAJE_C133-142.png");
        verify(eventos).publishEvent(any(CambioDatos.class));
    }

    @Test
    @DisplayName("Subir dos veces el mismo contenido con el mismo id no repite el guardado")
    void idempotente() throws Exception {
        UUID id = UUID.randomUUID();
        byte[] contenido = png(10, 10);
        Foto primera = servicio.subir(id, metadatos(null, null, null, null, null), contenido, "a.png", null, null);
        when(fotos.findById(id)).thenReturn(Optional.of(primera));

        Foto segunda = servicio.subir(id, metadatos(null, null, null, null, null), contenido, "a.png", null, null);

        assertThat(segunda).isSameAs(primera);
        verify(almacen, org.mockito.Mockito.times(2)).guardar(anyString(), any());
    }

    @Test
    @DisplayName("Un archivo que no es JPEG ni PNG se rechaza por su contenido, no por su nombre")
    void noImagen() {
        assertThatThrownBy(() -> servicio.subir(null, metadatos(null, null, null, null, null),
                "%PDF-1.7".getBytes(), "foto.jpg", null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Condición, tramo y cajas se validan antes de guardar")
    void validaciones() {
        assertThatThrownBy(() -> servicio.subir(null, metadatos("mojado", null, null, null, null), png(5, 5), "a.png", null, null))
                .hasMessageContaining("seco");
        assertThatThrownBy(() -> servicio.subir(null, metadatos(null, "20", "10", null, null), png(5, 5), "a.png", null, null))
                .hasMessageContaining("invertido");
        assertThatThrownBy(() -> servicio.subir(null, metadatos(null, null, null, 9, 3), png(5, 5), "a.png", null, null))
                .hasMessageContaining("cajas");
        assertThatThrownBy(() -> servicio.subir(null, new Metadatos(UUID.randomUUID(), null, null, null, null, null, null, null),
                png(5, 5), "a.png", null, null)).hasMessageContaining("sondaje");
    }

    @Test
    @DisplayName("Reconoce JPEG y PNG por sus primeros bytes")
    void tipo() throws Exception {
        assertThat(FotoService.tipoPorContenido(new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0})).isEqualTo("image/jpeg");
        assertThat(FotoService.tipoPorContenido(png(1, 1))).isEqualTo("image/png");
        assertThat(FotoService.tipoPorContenido(new byte[] {1, 2, 3, 4, 5})).isNull();
    }

    @Test
    @DisplayName("Sin miniatura se entrega el original")
    void miniaturaAusente() throws Exception {
        Foto f = new Foto();
        f.setClaveAlmacen("S/1.png");
        when(almacen.existe("S/1_min.jpg")).thenReturn(false);

        servicio.contenido(f, true);

        verify(almacen).abrir("S/1.png");
    }

    @Test
    @DisplayName("Eliminar es una baja lógica; después la foto ya no se entrega")
    void eliminar() {
        UUID id = UUID.randomUUID();
        Foto f = new Foto();
        f.setId(id);
        f.setSondajeId(SONDAJE);
        when(fotos.findById(id)).thenReturn(Optional.of(f));

        servicio.eliminar(id);

        assertThat(f.getEliminadoEn()).isNotNull();
        assertThatThrownBy(() -> servicio.obtener(id)).isInstanceOf(EntityNotFoundException.class);
        verify(fotos, never()).delete(any());
    }
}
