package com.viralsim.controllers;

import com.viralsim.models.Grafo;
import com.viralsim.services.GrafoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grafos")
public class GrafoController {
    
    private final GrafoService grafoService;

    public GrafoController(GrafoService grafoService) {
        this.grafoService = grafoService;
    }

    @PostMapping
    public ResponseEntity<Grafo> crearGrafo(@RequestParam int totalNodos) {
        return ResponseEntity.ok(grafoService.crearGrafo(totalNodos));
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
