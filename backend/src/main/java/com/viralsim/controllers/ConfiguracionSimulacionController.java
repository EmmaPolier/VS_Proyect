package com.viralsim.controllers;

import com.viralsim.models.ConfiguracionSimulacion;
import com.viralsim.services.ConfiguracionSimulacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/configuraciones")
public class ConfiguracionSimulacionController {

    private final ConfiguracionSimulacionService configService;

    public ConfiguracionSimulacionController(ConfiguracionSimulacionService configService) {
        this.configService = configService;
    }

    @PostMapping
    public ResponseEntity<ConfiguracionSimulacion> crear(@RequestParam int simulacionId,
            @RequestParam double probabilidadGlobal,
            @RequestParam int velocidadAnimacion) {
        return ResponseEntity.ok(configService.crear(simulacionId, probabilidadGlobal, velocidadAnimacion));
    }

    @GetMapping("/simulacion/{simulacionId}")
    public ResponseEntity<ConfiguracionSimulacion> obtenerPorSimulacion(@PathVariable int simulacionId) {
        return ResponseEntity.ok(configService.obtenerPorSimulacion(simulacionId));
    }
}
