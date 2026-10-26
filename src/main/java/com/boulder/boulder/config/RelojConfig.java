package com.boulder.boulder.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * El reloj de la aplicación, en la hora de Lima. Se inyecta en vez de llamar a
 * {@code LocalDate.now()} directamente: así «hoy» se puede fijar en las pruebas y
 * no depende de la zona horaria del servidor donde se despliegue.
 */
@Configuration
public class RelojConfig {

    @Bean
    public Clock reloj() {
        return Clock.system(ZoneId.of("America/Lima"));
    }
}
