package com.viralsim.controllers;

import com.viralsim.models.Nodo;
import com.viralsim.services.NodoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/nodos")
public class NodoController {

    private final NodoService nodoService;

    public NodoController(NodoService nodoService) {
        this.nodoService = nodoService;
    }

    @PostMapping
    public ResponseEntity<Nodo> crearNodo(@RequestParam int grafoId, @RequestParam String nombre) {
        return ResponseEntity.ok(nodoService.crearNodo(grafoId, nombre));
    }

    @GetMapping("/grafo/{grafoId}")
    public ResponseEntity<List<Nodo>> obtenerPorGrafo(@PathVariable int grafoId) {
        return ResponseEntity.ok(nodoService.obtenerPorGrafo(grafoId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Nodo> obtenerPorId(@PathVariable int id) {
        return ResponseEntity.ok(nodoService.obtenerPorId(id));
    }
}
