package com.boulder.boulder.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.boulder.boulder.dto.AvancePerforacionDTO;
import com.boulder.boulder.dto.AvancePersonalDTO;
import com.boulder.boulder.entities.AvancePerforacion;
import com.boulder.boulder.entities.AvancePersonal;
import com.boulder.boulder.entities.Maquina;
import com.boulder.boulder.entities.Plataforma;
import com.boulder.boulder.entities.Sondaje;
import com.boulder.boulder.repositories.AvancePerforacionRepository;
import com.boulder.boulder.repositories.EmpleadoRepository;
import com.boulder.boulder.repositories.MaquinaRepository;
import com.boulder.boulder.repositories.PlataformaRepository;
import com.boulder.boulder.repositories.SondajeRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * Registro del parte de perforación desde el formulario (RF05, CU15). Los
 * repositorios son dobles de prueba.
 */
@ExtendWith(MockitoExtension.class)
class AvancePerforacionServiceTest {

    @Mock private AvancePerforacionRepository avances;
    @Mock private SondajeRepository sondajes;
    @Mock private MaquinaRepository maquinas;
    @Mock private PlataformaRepository plataformas;
    @Mock private EmpleadoRepository empleados;

    private AvancePerforacionService servicio;

    private final UUID sondajeId = UUID.randomUUID();
    private final LocalDate hoy = LocalDate.of(2026, 10, 26);
    private Sondaje sondaje;
    private Maquina maquina;
    private Plataforma plataforma;

    @BeforeEach
    void preparar() {
        servicio = new AvancePerforacionServiceImpl(avances, sondajes, maquinas, plataformas, empleados);

        maquina = new Maquina();
        maquina.setId(UUID.randomUUID());
        maquina.setCodigo("C8C EPIROC");
        plataforma = new Plataforma();
        plataforma.setId(UUID.randomUUID());
        plataforma.setCodigo("PLT-08");
        sondaje = new Sondaje();
        sondaje.setId(sondajeId);
        sondaje.setCodigo("DDH-VERO-S-041-26");
        sondaje.setMaquina(maquina);
        sondaje.setPlataforma(plataforma);

        lenient().when(sondajes.findById(sondajeId)).thenReturn(Optional.of(sondaje));
        lenient().when(maquinas.findById(maquina.getId())).thenReturn(Optional.of(maquina));
        lenient().when(plataformas.findById(plataforma.getId())).thenReturn(Optional.of(plataforma));
        lenient().when(avances.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    /** Lo que manda el formulario: ids, sin los códigos de texto. */
    private AvancePerforacionDTO parte(String desde, String hasta, String avance) {
        AvancePerforacionDTO dto = new AvancePerforacionDTO();
        dto.setSondajeId(sondajeId);
        dto.setFecha(hoy);
        dto.setTurno("dia");
        dto.setDesde(new BigDecimal(desde));
        dto.setHasta(new BigDecimal(hasta));
        dto.setAvance(new BigDecimal(avance));
        dto.setMaquinaId(maquina.getId());
        dto.setPlataformaId(plataforma.getId());
        return dto;
    }

    private AvancePerforacion guardado(UUID id, String turno, String desde, String hasta) {
        AvancePerforacion e = new AvancePerforacion();
        e.setId(id);
        e.setSondaje(sondaje);
        e.setFecha(hoy);
        e.setTurno(turno);
        e.setDesde(new BigDecimal(desde));
        e.setHasta(new BigDecimal(hasta));
        e.setAvance(new BigDecimal(hasta).subtract(new BigDecimal(desde)));
        return e;
    }

    @Test
    @DisplayName("Un parte que cuadra se guarda con los códigos tomados del sondaje, la máquina y la plataforma")
    void guardaConCodigos() {
        servicio.guardar(parte("120.50", "135.20", "14.70"));

        ArgumentCaptor<AvancePerforacion> captor = ArgumentCaptor.forClass(AvancePerforacion.class);
        verify(avances).save(captor.capture());
        AvancePerforacion e = captor.getValue();
        assertThat(e.getSondajeCodigo()).isEqualTo("DDH-VERO-S-041-26");
        assertThat(e.getMaquinaCodigo()).isEqualTo("C8C EPIROC");
        assertThat(e.getPlataformaCodigo()).isEqualTo("PLT-08");
        assertThat(e.getEstado()).isEqualTo("registrado");
    }

    @Test
    @DisplayName("Si el avance no es hasta menos desde, no se guarda")
    void avanceNoCuadra() {
        assertThatThrownBy(() -> servicio.guardar(parte("120.50", "135.20", "17.40")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("14.70");
        verify(avances, never()).save(any());
    }

    @Test
    @DisplayName("El turno es día o noche")
    void turnoInvalido() {
        AvancePerforacionDTO dto = parte("0", "10", "10");
        dto.setTurno("tarde");
        assertThatThrownBy(() -> servicio.guardar(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("turno");
        verify(avances, never()).save(any());
    }

    @Test
    @DisplayName("El mismo turno del mismo sondaje no se carga dos veces")
    void duplicado() {
        when(avances.findBySondajeIdAndFecha(sondajeId, hoy))
                .thenReturn(List.of(guardado(UUID.randomUUID(), "dia", "120.5", "135.2")));

        assertThatThrownBy(() -> servicio.guardar(parte("120.50", "135.20", "14.70")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ya");
        verify(avances, never()).save(any());
    }

    @Test
    @DisplayName("El turno de noche del mismo día no es un duplicado")
    void otroTurno() {
        when(avances.findBySondajeIdAndFecha(sondajeId, hoy))
                .thenReturn(List.of(guardado(UUID.randomUUID(), "noche", "120.50", "135.20")));

        servicio.guardar(parte("120.50", "135.20", "14.70"));
        verify(avances).save(any());
    }

    @Test
    @DisplayName("Corregir un parte no choca consigo mismo")
    void corregir() {
        UUID id = UUID.randomUUID();
        AvancePerforacion existente = guardado(id, "dia", "120.50", "135.20");
        when(avances.findById(id)).thenReturn(Optional.of(existente));
        when(avances.findBySondajeIdAndFecha(sondajeId, hoy)).thenReturn(List.of(existente));

        AvancePerforacionDTO correccion = parte("120.50", "136.00", "15.50");
        AvancePerforacionDTO resultado = servicio.actualizar(id, correccion);

        assertThat(resultado.getHasta()).isEqualByComparingTo("136.00");
    }

    @Test
    @DisplayName("El parte siguiente arranca donde terminó el anterior, con su máquina y su dotación")
    void sugerenciaDesdeElUltimo() {
        AvancePerforacion ultimo = guardado(UUID.randomUUID(), "noche", "120.50", "135.20");
        ultimo.setMaquina(maquina);
        ultimo.setPlataforma(plataforma);
        ultimo.setResponsable("J. Quispe");
        ultimo.setLinea("HQ");
        AvancePersonal perforista = new AvancePersonal();
        perforista.setNombre("Mario Ccama");
        perforista.setCargo("Perforista");
        ultimo.getPersonal().add(perforista);
        when(avances.findFirstBySondajeIdOrderByHastaDescFechaDesc(sondajeId)).thenReturn(Optional.of(ultimo));

        AvancePerforacionDTO s = servicio.sugerencia(sondajeId);

        assertThat(s.getId()).isNull();
        assertThat(s.getSondajeCodigo()).isEqualTo("DDH-VERO-S-041-26");
        assertThat(s.getDesde()).isEqualByComparingTo("135.20");
        assertThat(s.getMaquinaId()).isEqualTo(maquina.getId());
        assertThat(s.getPlataformaId()).isEqualTo(plataforma.getId());
        assertThat(s.getResponsable()).isEqualTo("J. Quispe");
        assertThat(s.getLinea()).isEqualTo("HQ");
        assertThat(s.getPersonal()).extracting(AvancePersonalDTO::getNombre).containsExactly("Mario Ccama");
        assertThat(s.getPersonal()).extracting(AvancePersonalDTO::getId).containsOnlyNulls();
    }

    @Test
    @DisplayName("El primer parte de un sondaje arranca en 0 con la máquina y la plataforma del sondaje")
    void sugerenciaPrimerParte() {
        when(avances.findFirstBySondajeIdOrderByHastaDescFechaDesc(sondajeId)).thenReturn(Optional.empty());

        AvancePerforacionDTO s = servicio.sugerencia(sondajeId);

        assertThat(s.getDesde()).isEqualByComparingTo("0");
        assertThat(s.getMaquinaId()).isEqualTo(maquina.getId());
        assertThat(s.getPlataformaId()).isEqualTo(plataforma.getId());
        assertThat(s.getPersonal()).isEmpty();
    }

    @Test
    @DisplayName("Pedir el parte siguiente de un sondaje que no existe es un 404")
    void sugerenciaSondajeInexistente() {
        UUID otro = UUID.randomUUID();
        when(sondajes.findById(otro)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.sugerencia(otro)).isInstanceOf(EntityNotFoundException.class);
    }
}
