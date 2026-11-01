package com.boulder.boulder.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionOperations;

import com.boulder.boulder.dto.ResultadoSubidaDTO;
import com.boulder.boulder.entities.Quicklog;
import com.boulder.boulder.eventos.CambioDatos;
import com.boulder.boulder.repositories.AlteracionRepository;
import com.boulder.boulder.repositories.EstructuralRepository;
import com.boulder.boulder.repositories.LitologiaRepository;
import com.boulder.boulder.repositories.MineralizacionRepository;
import com.boulder.boulder.repositories.QuicklogRepository;

class LogueoServiceTest {

    private QuicklogRepository quicklogs;
    private ApplicationEventPublisher eventos;
    private LogueoServiceImpl servicio;

    @BeforeEach
    void preparar() {
        quicklogs = mock(QuicklogRepository.class);
        eventos = mock(ApplicationEventPublisher.class);
        servicio = new LogueoServiceImpl(mock(LitologiaRepository.class), mock(AlteracionRepository.class),
                mock(MineralizacionRepository.class), mock(EstructuralRepository.class), quicklogs,
                eventos, mock(JdbcTemplate.class), TransactionOperations.withoutTransaction());
    }

    private static Quicklog tramo(UUID id) {
        Quicklog q = new Quicklog();
        q.setId(id);
        q.setSondajeId(UUID.randomUUID());
        return q;
    }

    @Test
    @DisplayName("Lote completo: guarda todas las filas y no rechaza ninguna")
    void completo() {
        ResultadoSubidaDTO r = servicio.guardarQuicklog(List.of(tramo(UUID.randomUUID()), tramo(UUID.randomUUID())),
                "tablet-1", "geologo", "1.0");

        assertThat(r.guardadas()).isEqualTo(2);
        assertThat(r.completo()).isTrue();
        verify(eventos).publishEvent(any(CambioDatos.class));
    }

    @Test
    @DisplayName("Una fila que la base rechaza no tumba el lote: las demás se guardan")
    void filaRechazada() {
        UUID mala = UUID.randomUUID();
        when(quicklogs.saveAndFlush(argThat(q -> q != null && mala.equals(q.getId()))))
                .thenThrow(new DataIntegrityViolationException("viola uq_quicklog_tramo"));

        ResultadoSubidaDTO r = servicio.guardarQuicklog(
                List.of(tramo(UUID.randomUUID()), tramo(mala), tramo(UUID.randomUUID())), "tablet-1", "geologo", "1.0");

        assertThat(r.guardadas()).isEqualTo(2);
        assertThat(r.rechazadas()).singleElement().satisfies(x -> {
            assertThat(x.indice()).isEqualTo(1);
            assertThat(x.id()).isEqualTo(mala);
            assertThat(x.motivo()).contains("uq_quicklog_tramo");
        });
    }

    @Test
    @DisplayName("Una fila sin id se rechaza sola, con su posición")
    void sinId() {
        ResultadoSubidaDTO r = servicio.guardarQuicklog(List.of(tramo(null), tramo(UUID.randomUUID())),
                null, "geologo", null);

        assertThat(r.guardadas()).isEqualTo(1);
        assertThat(r.rechazadas()).singleElement().satisfies(x -> {
            assertThat(x.indice()).isZero();
            assertThat(x.id()).isNull();
        });
    }

    @Test
    @DisplayName("Las bajas se guardan antes que las altas")
    void bajasPrimero() {
        Quicklog alta = tramo(UUID.randomUUID());
        Quicklog baja = tramo(UUID.randomUUID());
        baja.setEliminadoEn(OffsetDateTime.now());

        servicio.guardarQuicklog(List.of(alta, baja), "tablet-1", "geologo", "1.0");

        InOrder orden = inOrder(quicklogs);
        orden.verify(quicklogs).saveAndFlush(baja);
        orden.verify(quicklogs).saveAndFlush(alta);
    }

    @Test
    @DisplayName("Si no se guardó nada, no se avisa a la web")
    void sinAviso() {
        ResultadoSubidaDTO r = servicio.guardarQuicklog(List.of(tramo(null)), null, null, null);

        assertThat(r.guardadas()).isZero();
        verify(eventos, never()).publishEvent(any());
    }
}
