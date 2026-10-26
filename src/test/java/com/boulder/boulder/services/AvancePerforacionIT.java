package com.boulder.boulder.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.boulder.boulder.dto.AvancePerforacionDTO;
import com.boulder.boulder.entities.Sondaje;
import com.boulder.boulder.repositories.SondajeRepository;

/**
 * El parte de perforación contra PostgreSQL: lo que un doble de Mockito no ve
 * (la secuencia del número interno, los CHECK de la tabla). Cada prueba se
 * deshace al terminar.
 */
@SpringBootTest
@Transactional
class AvancePerforacionIT {

    @Autowired
    private AvancePerforacionService servicio;

    @Autowired
    private SondajeRepository sondajes;

    private Sondaje sondaje;

    @BeforeEach
    void preparar() {
        sondaje = sondajes.findAll().stream().findFirst().orElseThrow();
    }

    /** Un tramo que no existe: muy por debajo del fondo de cualquier sondaje. */
    private AvancePerforacionDTO parte() {
        AvancePerforacionDTO dto = new AvancePerforacionDTO();
        dto.setSondajeId(sondaje.getId());
        dto.setFecha(LocalDate.of(2026, 10, 26));
        dto.setTurno("noche");
        dto.setDesde(new BigDecimal("9990.00"));
        dto.setHasta(new BigDecimal("9991.50"));
        dto.setAvance(new BigDecimal("1.50"));
        return dto;
    }

    @Test
    @DisplayName("El parte recién creado vuelve con el número interno que asignó la base")
    void numeroInterno() {
        AvancePerforacionDTO creado = servicio.guardar(parte());

        assertThat(creado.getId()).isNotNull();
        assertThat(creado.getNumeroInterno()).isNotNull().isPositive();
        assertThat(creado.getSondajeCodigo()).isEqualTo(sondaje.getCodigo());
    }

    @Test
    @DisplayName("El mismo turno no se guarda dos veces")
    void duplicado() {
        servicio.guardar(parte());

        assertThatThrownBy(() -> servicio.guardar(parte())).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("El borrador del turno siguiente arranca en el parte recién guardado")
    void sugerencia() {
        servicio.guardar(parte());

        assertThat(servicio.sugerencia(sondaje.getId()).getDesde()).isEqualByComparingTo("9991.50");
    }
}
