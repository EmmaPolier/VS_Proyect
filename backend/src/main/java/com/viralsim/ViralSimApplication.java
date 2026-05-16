package com.viralsim;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class ViralSimApplication {

    
    public static void main (String[] args) {
        SpringApplication.run(ViralSimApplication.class, args);
    }

    // Dentro de la clase ViralSimApplication, debajo del main:
        
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("http://localhost:5173", "http://localhost:3000") //AQUI SE AGREGA LOS PUERTOS DEL FRONTEND
                        .allowedMethods("GET", "POST", "PUT")
                        .allowedHeaders("*");
            }
        };
    }
}
