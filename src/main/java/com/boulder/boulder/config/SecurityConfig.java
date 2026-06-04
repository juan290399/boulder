package com.boulder.boulder.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor // Nos genera el constructor automático para jwtUtils
public class SecurityConfig {

    private final JwtUtils jwtUtils; // 👈 Inyectamos la utilidad de tokens
    private final AuthenticationConfiguration authenticationConfiguration; // 👈 Para obtener el mánager

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        
        // 1. Obtenemos el mánager ya construido
        AuthenticationManager authManager = authenticationManager();

        // 2. Instanciamos el filtro manualmente pasándole sus dependencias
        JwtAuthenticationFilter jwtAuthFilter = new JwtAuthenticationFilter(authManager, jwtUtils);
        
        // Asignamos la URL exacta en la que el filtro interceptará el POST (Tu ruta de Angular)
        jwtAuthFilter.setFilterProcessesUrl("/api/auth/login");

        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(request -> {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(List.of("http://localhost:4200"));
                config.setAllowedMethods(List.of("*"));
                config.setAllowedHeaders(List.of("*"));
                config.setAllowCredentials(true);
                return config;
            }))
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                // Permitimos el acceso libre al endpoint de login (lo maneja el filtro)
                .requestMatchers("/api/auth/login").permitAll() 
                .anyRequest().authenticated()
            )
            // 3. Añadimos el filtro instanciado en la cadena de seguridad
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}