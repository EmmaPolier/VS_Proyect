package com.viralsim.controllers;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.viralsim.dto.MetricasSimulacion;
import com.viralsim.engine.MotorSimulacion;
import com.viralsim.models.NodoSimulacion;
import com.viralsim.models.PasoSimulacion;
import com.viralsim.models.Simulacion;
import com.viralsim.services.SimulacionService;

@RestController
@RequestMapping("/api/simulaciones")
public class SimulacionController {

    private final SimulacionService simulacionService;
    @Autowired
    private MotorSimulacion motorSimulacion;

    public SimulacionController(SimulacionService simulacionService) {
        this.simulacionService = simulacionService;
    }

    @PostMapping
    public ResponseEntity<Simulacion> crear(@RequestParam int grafoId,
            @RequestParam int modeloId,
            @RequestParam int nodoSemillaId) {
        return ResponseEntity.ok(simulacionService.crearSimulacion(grafoId, modeloId, nodoSemillaId));
    }

    @PostMapping("/{id}/ejecutar")
    public ResponseEntity<?> ejecutarSimulacion(@PathVariable int id) {
        try {
            MotorSimulacion.ResultadoSimulacion resultado = motorSimulacion.ejecutar(id);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.status(404)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Simulacion>> obtenerTodas() {
        return ResponseEntity.ok(simulacionService.obtenerTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Simulacion> obtenerPorId(@PathVariable int id) {
        return ResponseEntity.ok(simulacionService.obtenerPorId(id));
    }

    /**
     * Obtiene las métricas de una simulación ejecutada.
     * 
     * @param id ID de la simulación
     * @return DTO con las métricas principales (total pasos, informados, alcance,
     *         etc.)
     */
    @GetMapping("/{id}/metricas")
    public ResponseEntity<MetricasSimulacion> obtenerMetricas(@PathVariable int id) {
        return ResponseEntity.ok(simulacionService.obtenerMetricas(id));
    }

    /**
     * Obtiene todos los pasos de una simulación.
     * 
     * @param id ID de la simulación
     * @return Lista de PasoSimulacion ordenados por numeroPaso
     */
    @GetMapping("/{id}/pasos")
    public ResponseEntity<List<PasoSimulacion>> obtenerPasos(@PathVariable int id) {
        return ResponseEntity.ok(simulacionService.obtenerPasos(id));
    }

    /**
     * Obtiene los nodos simulación de un paso específico.
     * 
     * @param id   ID de la simulación
     * @param paso Número del paso a consultar
     * @return Lista de NodoSimulacion del paso
     */
    @GetMapping("/{id}/nodo-simulacion")
    public ResponseEntity<List<NodoSimulacion>> obtenerNodoSimulacionPorPaso(@PathVariable int id,
            @RequestParam int paso) {
        return ResponseEntity.ok(simulacionService.obtenerNodoSimulacionPorPaso(id, paso));
    }

    /**
     * Compara los 3 modelos de propagación sobre el mismo grafo y nodo inicial.
     * Ejecuta simulaciones con Viral, Cascada Independiente y Threshold.
     * 
     * @param grafoId       ID del grafo
     * @param nodoSemillaId ID del nodo inicial
     * @return Array con 3 ResultadoSimulacion (Viral, Cascada, Threshold) para
     *         comparar
     */
    @PostMapping("/comparar")
    public ResponseEntity<?> compararModelos(@RequestParam int grafoId, @RequestParam int nodoSemillaId) {
        try {
            List<MotorSimulacion.ResultadoSimulacion> resultados = motorSimulacion.compararModelos(grafoId,
                    nodoSemillaId);
            return ResponseEntity.ok(resultados);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }
}
