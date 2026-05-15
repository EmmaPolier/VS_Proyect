package com.viralsim.metrics;

import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.alg.scoring.BetweennessCentrality;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.viralsim.models.Arista;
import com.viralsim.models.Grafo;
import com.viralsim.models.Nodo;
import com.viralsim.repositories.NodoRepository;

/**
 * Servicio para calcular métricas de centralidad en grafos de red.
 * Utiliza JGraphT para los cálculos matemáticos complejos.
 */
@Service
public class CalculadorMetricas {

    @Autowired
    private NodoRepository nodoRepository;

    /**
     * Calcula la centralidad de grado normalizada para todos los nodos del grafo.
     * Fórmula: grado / (n - 1), donde n es el número total de nodos.
     *
     * @param grafo El grafo principal (necesario para obtener totalNodos)
     * @param nodos Lista de todos los nodos del grafo
     * @param aristas Lista de todas las aristas del grafo
     */
    public void calcularCentralidadGrado(Grafo grafo, List<Nodo> nodos, List<Arista> aristas) {
        // Contar grado de cada nodo
        Map<Integer, Integer> gradoPorNodo = new java.util.HashMap<>();
        
        // Inicializar contadores en 0
        for (Nodo nodo : nodos) {
            gradoPorNodo.put(nodo.getId(), 0);
        }

        // Contar aristas para cada nodo (grafo no dirigido, cada arista cuenta para ambos extremos)
        for (Arista arista : aristas) {
            int nodoOrigen = arista.getNodoOrigen().getId();
            int nodoDestino = arista.getNodoDestino().getId();
            
            gradoPorNodo.put(nodoOrigen, gradoPorNodo.get(nodoOrigen) + 1);
            gradoPorNodo.put(nodoDestino, gradoPorNodo.get(nodoDestino) + 1);
        }

        // Normalizar y asignar: centralidadGrado = grado / (n - 1)
        int totalNodos = grafo.getTotalNodos();
        for (Nodo nodo : nodos) {
            int grado = gradoPorNodo.getOrDefault(nodo.getId(), 0);
            double centralidadNormalizada = totalNodos > 1 ? (double) grado / (totalNodos - 1) : 0.0;
            nodo.setCentralidadGrado(centralidadNormalizada);
            nodoRepository.save(nodo);
        }
    }

    /**
     * Calcula la centralidad de intermediación (betweenness centrality) para todos los nodos.
     * Utiliza JGraphT para el cálculo eficiente del algoritmo de Brandes.
     *
     * @param grafo El grafo principal
     * @param nodos Lista de todos los nodos del grafo
     * @param aristas Lista de todas las aristas del grafo
     */
    public void calcularBetweennessCentrality(Grafo grafo, List<Nodo> nodos, List<Arista> aristas) {
        // Construir grafo de JGraphT (no dirigido)
        Graph<Integer, DefaultEdge> jgraph = new SimpleGraph<>(DefaultEdge.class);

        // Agregar vértices (nodoId)
        for (Nodo nodo : nodos) {
            jgraph.addVertex(nodo.getId());
        }

        // Agregar aristas (sin duplicar, ya que es no dirigido)
        for (Arista arista : aristas) {
            jgraph.addEdge(arista.getNodoOrigen().getId(), arista.getNodoDestino().getId());
        }

        // Calcular betweenness usando JGraphT
        BetweennessCentrality<Integer, DefaultEdge> betweenness = 
            new BetweennessCentrality<>(jgraph);
        Map<Integer, Double> scores = betweenness.getScores();

        // Normalizar: betweenness / ((n-1) * (n-2) / 2)
        // Esta es la forma normalizada entre 0 y 1
        int n = grafo.getTotalNodos();
        double normalizador = n > 2 ? ((double) (n - 1) * (n - 2) / 2.0) : 1.0;

        // Asignar valores a los nodos
        for (Nodo nodo : nodos) {
            double betweennessRaw = scores.getOrDefault(nodo.getId(), 0.0);
            double betweennessNormalizado = betweennessRaw / normalizador;
            nodo.setBetweenness(betweennessNormalizado);
            nodoRepository.save(nodo);
        }
    }

    /**
     * Método auxiliar: calcula ambas métricas en una sola llamada.
     * Útil para la fase de generación de la red (Watts-Strogatz).
     *
     * @param grafo El grafo principal
     * @param nodos Lista de todos los nodos del grafo
     * @param aristas Lista de todas las aristas del grafo
     */
    public void calcularTodasLasMetricas(Grafo grafo, List<Nodo> nodos, List<Arista> aristas) {
        calcularCentralidadGrado(grafo, nodos, aristas);
        calcularBetweennessCentrality(grafo, nodos, aristas);
    }
}
