package com.viralsim.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.viralsim.models.Grafo;
import com.viralsim.services.GrafoService;
import com.viralsim.services.MetricasService;
import com.viralsim.utils.WattsStrogatzGenerator;

@RestController
@RequestMapping("/api/grafos")
public class GrafoController {

    private final GrafoService grafoService;
    private final WattsStrogatzGenerator generator;
    private final MetricasService metricasService;

    public GrafoController(GrafoService grafoService, WattsStrogatzGenerator generator,
            MetricasService metricasService) {
        this.grafoService = grafoService;
        this.generator = generator;
        this.metricasService = metricasService;
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

    /**
     * Calcula las métricas de un grafo (betweenness centrality y grado).
     * Actualiza todos los nodos del grafo con sus valores calculados.
     * 
     * @param id ID del grafo
     * @return Mensaje de confirmación
     */
    @PostMapping("/{id}/calcular-metricas")
    public ResponseEntity<Map<String, String>> calcularMetricas(@PathVariable int id) {
        metricasService.calcularMetricasGrafo(id);
        return ResponseEntity
                .ok(Map.of("mensaje", "Métricas calculadas para el grafo " + id, "grafoId", String.valueOf(id)));
    }
}
