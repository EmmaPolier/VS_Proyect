package com.viralsim.controllers;

import java.util.List;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.viralsim.models.Nodo;
import com.viralsim.services.NodoService;

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

    /**
     * Actualiza un nodo existente.
     * @param id ID del nodo
     * @param nodoActualizado Objeto Nodo con los campos a actualizar
     * @return El nodo actualizado
     */
    @PutMapping("/{id}")
        public ResponseEntity<Nodo> actualizarNodo(@PathVariable int id, @RequestBody Nodo nodoActualizado) {
            return ResponseEntity.ok(nodoService.actualizarNodo(id, nodoActualizado));
        }

    /**
     * Obtiene los nodos de un grafo ordenados por centralidad de grado (top primero).
     * @param grafoId ID del grafo
     * @return Lista de nodos ordenada por centralidadGrado descendente
     */
    @GetMapping("/grafo/{grafoId}/top-grado")
    public ResponseEntity<List<Nodo>> obtenerTopPorGrado(@PathVariable int grafoId) {
        return ResponseEntity.ok(nodoService.obtenerTopPorGrado(grafoId));
    }

    /**
     * Obtiene los nodos de un grafo ordenados por betweenness centrality (top primero).
     * @param grafoId ID del grafo
     * @return Lista de nodos ordenada por betweenness descendente
     */
    @GetMapping("/grafo/{grafoId}/top-betweenness")
    public ResponseEntity<List<Nodo>> obtenerTopPorBetweenness(@PathVariable int grafoId) {
        return ResponseEntity.ok(nodoService.obtenerTopPorBetweenness(grafoId));
    }
}
