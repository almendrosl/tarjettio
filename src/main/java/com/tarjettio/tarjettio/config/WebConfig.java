package com.tarjettio.tarjettio.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración web para CORS y otros ajustes web
 */
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // Si tu frontend corre en un puerto diferente, ajústalo aquí.
                // Si frontend y backend corren en el mismo puerto (8080), puedes omitir esta línea.
                .allowedOrigins("http://localhost:3000", "http://localhost:8080")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true) // Crucial para que las cookies de sesión funcionen
                .maxAge(3600);
    }
}