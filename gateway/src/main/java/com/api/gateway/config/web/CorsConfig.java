package com.api.gateway.config.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * CORS centralizado en el gateway. Como ahora el frontend solo habla con un único origen
 * (localhost:8080), basta con permitir aquí el origen del navegador (Angular) y olvidarnos
 * de configurar CORS en auth y notes.
 *
 * Spring Security recoge este bean (gracias a http.cors(...)) y con él responde también al
 * preflight OPTIONS, sin que ese OPTIONS necesite token.
 */
@Configuration
public class CorsConfig {

    /** Orígenes permitidos del frontend; configurable en application.yml (coma-separados). */
    @Value("${app.cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        // Permite que el navegador envíe credenciales (cookies / Authorization) en peticiones cross-origin.
        config.setAllowCredentials(true);
        // Expone el header Authorization por si el frontend necesita leerlo en alguna respuesta.
        config.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
