package com.viralsim.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.viralsim.models.Arista;
import com.viralsim.services.AristaService;

@RestController
@RequestMapping("/api/aristas")
public class AristaController {

    private final AristaService aristaService;

    public AristaController(AristaService aristaService) {
        this.aristaService = aristaService;
    }

    @PostMapping
    public ResponseEntity<Arista> crear(@RequestParam int nodoOrigenId,
            @RequestParam int nodoDestinoId) {
        return ResponseEntity.ok(aristaService.crearArista(nodoOrigenId, nodoDestinoId));
    }

    @GetMapping
    public ResponseEntity<List<Arista>> obtenerTodas() {
        return ResponseEntity.ok(aristaService.obtenerTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Arista> obtenerPorId(@PathVariable int id) {
        return ResponseEntity.ok(aristaService.obtenerPorId(id));
    }

    /**
     * Obtiene todas las aristas de un grafo específico.
     * @param grafoId ID del grafo
     * @return Lista de aristas del grafo
     */
    @GetMapping("/grafo/{grafoId}")
    public ResponseEntity<List<Arista>> obtenerPorGrafo(@PathVariable int grafoId) {
        return ResponseEntity.ok(aristaService.obtenerPorGrafo(grafoId));
    }
}
