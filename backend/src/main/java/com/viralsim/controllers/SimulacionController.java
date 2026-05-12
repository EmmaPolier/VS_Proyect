package com.viralsim.controllers;

import com.viralsim.models.Simulacion;
import com.viralsim.services.SimulacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/simulaciones")
public class SimulacionController {

    private final SimulacionService simulacionService;

    public SimulacionController(SimulacionService simulacionService) {
        this.simulacionService = simulacionService;
    }

    @PostMapping
    public ResponseEntity<Simulacion> crear(@RequestParam int grafoId,
            @RequestParam int modeloId,
            @RequestParam int nodoSemillaId) {
        return ResponseEntity.ok(simulacionService.crearSimulacion(grafoId, modeloId, nodoSemillaId));
    }

    @GetMapping
    public ResponseEntity<List<Simulacion>> obtenerTodas() {
        return ResponseEntity.ok(simulacionService.obtenerTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Simulacion> obtenerPorId(@PathVariable int id) {
        return ResponseEntity.ok(simulacionService.obtenerPorId(id));
    }
}
