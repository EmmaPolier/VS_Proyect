package com.viralsim.services;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jgrapht.Graph;
import org.jgrapht.alg.scoring.BetweennessCentrality;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.DefaultEdge;
import org.springframework.stereotype.Service;

import com.viralsim.models.Arista;
import com.viralsim.models.Grafo;
import com.viralsim.models.Nodo;
import com.viralsim.repositories.AristaRepository;
import com.viralsim.repositories.GrafoRepository;
import com.viralsim.repositories.NodoRepository;

@Service
public class MetricasService {

    private final GrafoRepository grafoRepository;
    private final NodoRepository nodoRepository;
    private final AristaRepository aristaRepository;

    public MetricasService(GrafoRepository grafoRepository,
            NodoRepository nodoRepository,
            AristaRepository aristaRepository) {
        this.grafoRepository = grafoRepository;
        this.nodoRepository = nodoRepository;
        this.aristaRepository = aristaRepository;
    }

    /**
     * Calcula las métricas de un grafo (betweenness centrality y grado).
     * Actualiza los nodos con sus valores calculados.
     * 
     * @param grafoId ID del grafo
     */
    public void calcularMetricasGrafo(int grafoId) {
        // Cargar el grafo
        Grafo grafo = grafoRepository.findById(grafoId)
                .orElseThrow(() -> new RuntimeException("Grafo no encontrado: " + grafoId));

        // Cargar todos los nodos del grafo
        List<Nodo> nodos = nodoRepository.findByGrafo_Id(grafoId);
        Map<Integer, Nodo> nodosMap = nodos.stream()
                .collect(Collectors.toMap(Nodo::getId, n -> n));

        // Cargar todas las aristas del grafo
        List<Arista> aristas = aristaRepository.findByGrafo_Id(grafoId);

        // Crear grafo con JGraphT (no dirigido, sin pesos)
        Graph<Integer, DefaultEdge> jgraph = new SimpleGraph<>(DefaultEdge.class);

        // Agregar nodos al grafo de JGraphT
        for (Nodo nodo : nodos) {
            jgraph.addVertex(nodo.getId());
        }

        // Agregar aristas al grafo de JGraphT (bidireccionales)
        for (Arista arista : aristas) {
            int nodoOrigen = arista.getNodoOrigen().getId();
            int nodoDestino = arista.getNodoDestino().getId();
            jgraph.addEdge(nodoOrigen, nodoDestino);
            jgraph.addEdge(nodoDestino, nodoOrigen); // bidireccional
        }

        // Calcular betweenness centrality
        BetweennessCentrality<Integer, DefaultEdge> betweennessCentral = new BetweennessCentrality<>(jgraph);
        Map<Integer, Double> betweennessMap = betweennessCentral.getScores();

        // Calcular grado de cada nodo (número de vecinos)
        Map<Integer, Integer> gradoMap = nodos.stream()
                .collect(Collectors.toMap(
                        Nodo::getId,
                        nodo -> {
                            int grado = 0;
                            for (Arista arista : aristas) {
                                if (arista.getNodoOrigen().getId().equals(nodo.getId()) ||
                                        arista.getNodoDestino().getId().equals(nodo.getId())) {
                                    grado++;
                                }
                            }
                            return grado;
                        }));

        // Calcular centralidad de grado normalizada (grado / (n-1))
        int n = nodos.size();
        Map<Integer, Double> centralidadGradoMap = gradoMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> n > 1 ? (double) entry.getValue() / (n - 1) : 0.0));

        // Actualizar nodos con las métricas calculadas
        for (Nodo nodo : nodos) {
            int nodoId = nodo.getId();
            double betweenness = betweennessMap.getOrDefault(nodoId, 0.0);
            double centralidadGrado = centralidadGradoMap.getOrDefault(nodoId, 0.0);

            nodo.setBetweenness(betweenness);
            nodo.setCentralidadGrado(centralidadGrado);
            nodoRepository.save(nodo);
        }
    }
}
