package com.stocksphere.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Global CORS configuration.
 *
 * Allowed origins are read from the cors.allowed-origins property (comma-separated).
 * Override at runtime via the CORS_ALLOWED_ORIGINS environment variable.
 * Only GET and OPTIONS are permitted — the API has no mutation endpoints.
 */
@Configuration
public class WebConfig {

    /**
     * Spring auto-splits a comma-separated property value into String[].
     * Example: CORS_ALLOWED_ORIGINS=https://app.example.com,https://admin.example.com
     */
    @Value("${cors.allowed-origins:http://localhost:8080}")
    private String[] allowedOrigins;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(allowedOrigins)
                        .allowedMethods("GET", "OPTIONS")
                        .allowedHeaders("Content-Type", "Accept")
                        .maxAge(3600);
            }
        };
    }
}
