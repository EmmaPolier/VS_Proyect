package com.viralsim;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class ViralSimApplication {

    public static void main(String[] args) {
        SpringApplication.run(ViralSimApplication.class, args);
    }

    // Dentro de la clase ViralSimApplication, debajo del main:

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins(
                                "http://localhost:5500", // Live Server VS Code
                                "http://localhost:3000", // npm dev servers
                                "http://localhost:5173", // Vite
                                "http://127.0.0.1:5500", // Live Server alternativo
                                "http://127.0.0.1:3000", // localhost alternativo
                                "http://127.0.0.1:5173" // Vite alternativo
                )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}
