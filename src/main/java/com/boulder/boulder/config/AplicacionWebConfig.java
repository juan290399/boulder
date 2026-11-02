package com.boulder.boulder.config;

import java.io.IOException;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

/**
 * Sirve la aplicación Angular desde {@code classpath:/static/}, empaquetada en
 * el mismo jar que el API. Las rutas del cliente ({@code /reportes},
 * {@code /sondajes}…) no son archivos: devuelven {@code index.html} y el router
 * de Angular resuelve la pantalla. Un archivo con extensión que no existe sigue
 * siendo 404, y {@code /api} y {@code /actuator} nunca caen aquí.
 */
@Configuration
public class AplicacionWebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String ruta, Resource ubicacion) throws IOException {
                        Resource pedido = ubicacion.createRelative(ruta);
                        if (!ruta.isEmpty() && pedido.exists() && pedido.isReadable()) {
                            return pedido;
                        }
                        if (esRutaDelCliente(ruta)) {
                            Resource indice = ubicacion.createRelative("index.html");
                            return indice.exists() ? indice : null;
                        }
                        return null;
                    }
                });
    }

    /** Una ruta del router de Angular: no es del API ni pide un archivo con extensión. */
    static boolean esRutaDelCliente(String ruta) {
        String ultimo = ruta.substring(ruta.lastIndexOf('/') + 1);
        return !ruta.startsWith("api/") && !ruta.startsWith("actuator") && !ultimo.contains(".");
    }
}
