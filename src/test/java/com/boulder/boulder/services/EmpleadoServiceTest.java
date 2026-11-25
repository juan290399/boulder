package com.boulder.boulder.services;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.boulder.boulder.dto.EmpleadoDTO;
import com.boulder.boulder.repositories.CargoRepository;
import com.boulder.boulder.repositories.EmpleadoRepository;
import com.boulder.boulder.repositories.EmpresaRepository;

@ExtendWith(MockitoExtension.class)
class EmpleadoServiceTest {

    @Mock
    private EmpleadoRepository empleados;

    @Mock
    private CargoRepository cargos;

    @Mock
    private EmpresaRepository empresas;

    @InjectMocks
    private EmpleadoServiceImpl servicio;

    private static EmpleadoDTO datos() {
        EmpleadoDTO dto = new EmpleadoDTO();
        dto.setCodigo("PERF-JQUI");
        dto.setNombres("Juan");
        dto.setApellidos("Quispe");
        return dto;
    }

    @Test
    @DisplayName("La fecha de salida no puede ser anterior a la de ingreso")
    void fechasEnOrden() {
        EmpleadoDTO dto = datos();
        dto.setFechaIngreso(LocalDate.of(2026, 9, 1));
        dto.setFechaSalida(LocalDate.of(2026, 8, 15));

        assertThatThrownBy(() -> servicio.guardar(dto))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("fecha de salida");
        verify(empleados, never()).save(any());
    }

    @Test
    @DisplayName("Un código de más de 15 caracteres o nombres de más de 80 se rechazan antes de llegar a la base")
    void largosMaximos() {
        EmpleadoDTO dto = datos();
        dto.setCodigo("PERF-DEMASIADO-LARGO");
        dto.setNombres("N".repeat(81));

        assertThatThrownBy(() -> servicio.guardar(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("15").hasMessageContaining("80");
        verify(empleados, never()).save(any());
    }
}
