package com.boulder.boulder.config;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;

import com.google.common.base.Ticker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtils jwtUtils;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final CuentasActivas cuentasActivas;

    /**
     * Orígenes del frontend autorizados. Por defecto, el servidor de desarrollo
     * de Angular; en un despliegue se fija con la propiedad o la variable
     * BOULDER_CORS_ORIGENES, separados por coma.
     */
    @Value("${boulder.cors.origenes:http://localhost:4200}")
    private List<String> origenesCors;

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Contraseñas con BCrypt, sin obligar a resetear las que ya existen.
     *
     * <p>
     * El codificador delegante guarda cada hash con su algoritmo por delante
     * ({@code {bcrypt}$2a$10$...}), que es lo que permite cambiar de algoritmo
     * en el futuro sin romper las cuentas viejas. Las contraseñas cargadas antes
     * de este cambio están en texto plano y sin prefijo; para ellas se usa
     * {@code NoOpPasswordEncoder} solo al comparar, nunca al guardar.
     * </p>
     *
     * <p>
     * La migración ocurre sola: {@link MyUserDetailsService} implementa
     * {@code UserDetailsPasswordService}, así que cuando alguien entra con una
     * contraseña vieja Spring la vuelve a codificar con BCrypt y la guarda. No
     * hay script de migración ni ventana de mantenimiento.
     * </p>
     */
    @Bean
    @SuppressWarnings("deprecation")
    public PasswordEncoder passwordEncoder() {
        DelegatingPasswordEncoder codificador =
                (DelegatingPasswordEncoder) PasswordEncoderFactories.createDelegatingPasswordEncoder();
        codificador.setDefaultPasswordEncoderForMatches(NoOpPasswordEncoder.getInstance());
        return codificador;
    }

    /**
     * Cinco fallos seguidos bloquean la cuenta quince minutos desde el último
     * intento. Ver {@link LimiteIntentosLogin}.
     */
    @Bean
    public LimiteIntentosLogin limiteIntentosLogin() {
        return new LimiteIntentosLogin(5, Duration.ofMinutes(15), Ticker.systemTicker());
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        
        AuthenticationManager authManager = authenticationManager();

        JwtAuthenticationFilter jwtAuthFilter = new JwtAuthenticationFilter(authManager, jwtUtils, limiteIntentosLogin());
        jwtAuthFilter.setFilterProcessesUrl("/api/auth/login");

        JwtAuthorizationFilter jwtAuthorizationFilter = new JwtAuthorizationFilter(jwtUtils, cuentasActivas);

        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(request -> {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(origenesCors);
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
                config.setAllowedHeaders(List.of("*"));
                config.setAllowCredentials(true);
                return config;
            }))
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                // El preflight va primero: sin token no puede satisfacer hasRole(),
                // y si cae ahí el navegador aborta la petición real con un error CORS
                // que no dice nada sobre la causa.
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // El flujo de /api/eventos (SSE) se autoriza al abrirse. Cuando
                // termina, el contenedor vuelve a despachar la petición en modo
                // ASYNC y ahí ya no hay token que revisar: sin esta regla, cada
                // cierre de conexión deja un AccessDenied en el registro.
                .dispatcherTypeMatchers(DispatcherType.ASYNC, DispatcherType.ERROR).permitAll()

                // Login (atendido por JwtAuthenticationFilter) y auto-registro.
                .requestMatchers("/api/auth/**").permitAll()

                // Monitoreo: la salud la consulta el balanceador o el script de
                // vigilancia sin sesión; el detalle y las métricas, solo ADMIN.
                .requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/actuator/**").hasRole("ADMIN")

                // El catálogo de personal se lee desde los partes de perforación
                // y de logueo para poblar sus desplegables, así que la lectura
                // queda abierta a cualquier sesión. Va ANTES de la regla general
                // porque la primera coincidencia es la que manda. Las escrituras
                // caen en la regla de abajo y además llevan @PreAuthorize.
                .requestMatchers(HttpMethod.GET, "/api/base/empleados").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/base/empleados/**").authenticated()

                // Administración de cuentas, roles y datos maestros del esquema
                // `base`. Con `authenticated()` a secas cualquier VISITANTE con
                // token válido podía listar usuarios y desactivar cuentas: el
                // guard del frontend no protege nada, la puerta real es esta.
                .requestMatchers("/api/base/**").hasRole("ADMIN")

                // Lectura con cualquier sesión; escritura según el rol (PermisosEscritura).
                .requestMatchers("/api/operacional/**", "/api/logueo/**", "/api/importacion/**", "/api/fotos/**")
                    .access(SecurityConfig::escrituraPorRol)

                // La aplicación Angular empaquetada en el jar (AplicacionWebConfig):
                // el HTML y los archivos estáticos no llevan datos; los datos los
                // pide la aplicación al API con su token.
                .requestMatchers(SecurityConfig::esAplicacionWeb).permitAll()

                .anyRequest().authenticated()
            )
            
            .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)
            
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
 
        return http.build();
    }

    /** Un GET que no va al API ni al monitoreo: la aplicación web o uno de sus archivos. */
    private static boolean esAplicacionWeb(HttpServletRequest peticion) {
        String ruta = peticion.getRequestURI().substring(peticion.getContextPath().length());
        return "GET".equals(peticion.getMethod()) && !ruta.startsWith("/api/") && !ruta.startsWith("/actuator");
    }

    private static AuthorizationDecision escrituraPorRol(Supplier<? extends Authentication> autenticacion,
                                                         RequestAuthorizationContext contexto) {
        Authentication a = autenticacion.get();
        if (a == null || !a.isAuthenticated() || a instanceof AnonymousAuthenticationToken) {
            return new AuthorizationDecision(false);
        }
        HttpServletRequest peticion = contexto.getRequest();
        String ruta = peticion.getRequestURI().substring(peticion.getContextPath().length());
        Set<String> roles = a.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
        return new AuthorizationDecision(PermisosEscritura.permite(peticion.getMethod(), ruta, roles));
    }
}
