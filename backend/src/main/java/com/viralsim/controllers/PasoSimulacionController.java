package com.viralsim.controllers;

import com.viralsim.models.PasoSimulacion;
import com.viralsim.services.PasoSimulacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pasos")
public class PasoSimulacionController {

    private final PasoSimulacionService pasoService;

    public PasoSimulacionController(PasoSimulacionService pasoService) {
        this.pasoService = pasoService;
    }

    @PostMapping
    public ResponseEntity<PasoSimulacion> crear(@RequestParam int simulacionId,
            @RequestParam int numeroPaso,
            @RequestParam int nuevosInformados,
            @RequestParam int totalActivos,
            @RequestParam int totalResistentes) {
        return ResponseEntity.ok(pasoService.crear(simulacionId, numeroPaso,
                nuevosInformados, totalActivos, totalResistentes));
    }

    @GetMapping("/simulacion/{simulacionId}")
    public ResponseEntity<List<PasoSimulacion>> obtenerPorSimulacion(@PathVariable int simulacionId) {
        return ResponseEntity.ok(pasoService.obtenerPorSimulacion(simulacionId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PasoSimulacion> obtenerPorId(@PathVariable int id) {
        return ResponseEntity.ok(pasoService.obtenerPorId(id));
    }
}
