package com.viralsim.controllers;

import com.viralsim.models.Grafo;
import com.viralsim.services.GrafoService;
import com.viralsim.utils.WattsStrogatzGenerator;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grafos")
public class GrafoController {
    
    private final GrafoService grafoService;
    private final WattsStrogatzGenerator generator;

    public GrafoController(GrafoService grafoService, WattsStrogatzGenerator generator) {
        this.grafoService = grafoService;
        this.generator = generator;
    }

    @PostMapping("/crear")
    public ResponseEntity<Grafo> crearGrafo() {
        return ResponseEntity.ok(generator.crearGrafo());
    }

    @GetMapping
    public ResponseEntity<List<Grafo>> obtenerTodos() {
        return ResponseEntity.ok(grafoService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Grafo> obtenerPorId(@PathVariable int id) {
        return ResponseEntity.ok(grafoService.obtenerPorId(id));
    }
}
