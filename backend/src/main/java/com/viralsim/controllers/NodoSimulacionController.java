package com.viralsim.controllers;

import com.viralsim.models.NodoSimulacion;
import com.viralsim.services.NodoSimulacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/nodo-simulacion")
public class NodoSimulacionController {

    private final NodoSimulacionService nodoSimulacionService;

    public NodoSimulacionController(NodoSimulacionService nodoSimulacionService) {
        this.nodoSimulacionService = nodoSimulacionService;
    }

    @PostMapping
    public ResponseEntity<NodoSimulacion> crear(@RequestParam int simulacionId,
            @RequestParam int pasoId,
            @RequestParam int nodoId,
            @RequestParam int estadoId,
            @RequestParam(required = false) Integer pasoInfeccion) {
        return ResponseEntity.ok(nodoSimulacionService.crear(simulacionId, pasoId, nodoId, estadoId, pasoInfeccion));
    }

    @GetMapping("/paso/{pasoId}")
    public ResponseEntity<List<NodoSimulacion>> obtenerPorPaso(@PathVariable int pasoId) {
        return ResponseEntity.ok(nodoSimulacionService.obtenerPorPaso(pasoId));
    }
}
