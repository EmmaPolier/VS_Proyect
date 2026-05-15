package com.viralsim.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.viralsim.models.EstadoCatalogo;
import com.viralsim.models.Grafo;
import com.viralsim.models.Nodo;
import com.viralsim.repositories.EstadoCatalogoRepository;
import com.viralsim.repositories.GrafoRepository;
import com.viralsim.repositories.NodoRepository;

@Service
public class NodoService {
    
    private final NodoRepository nodoRepository;
    private final GrafoRepository grafoRepository;
    private final EstadoCatalogoRepository estadoRepository;

    public NodoService(NodoRepository nodoRepository, GrafoRepository grafoRepository, EstadoCatalogoRepository estadoRepository) {
        this.nodoRepository = nodoRepository;
        this.grafoRepository = grafoRepository;
        this.estadoRepository = estadoRepository;
    }

    public Nodo crearNodo(int grafoId, String nombre) {
        Grafo grafo = grafoRepository.findById(grafoId)
                .orElseThrow(() -> new RuntimeException("Grafo no encontrado: " + grafoId));
        EstadoCatalogo estadoInicial = estadoRepository.findById(0)
                .orElseThrow(() -> new RuntimeException("Estado NO_INFORMADO no esncontrado"));
        
        Nodo nodo = new Nodo();
        nodo.setGrafo(grafo);
        nodo.setNombre(nombre);
        nodo.setEstado(estadoInicial);
        return nodoRepository.save(nodo);
    }

    public List<Nodo> obtenerPorGrafo(int grafoId) {
        return nodoRepository.findAll().stream()
                .filter(n -> n.getGrafo().getId().equals(grafoId))
                .toList();
    }

    public Nodo obtenerPorId(int id) {
        return nodoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nodo no encontrado: " + id));
    }

    /**
     * Actualiza un nodo existente con los nuevos valores.
     * @param id ID del nodo
     * @param nodoActualizado Objeto con los campos a actualizar
     * @return El nodo actualizado
     */
    public Nodo actualizarNodo(int id, Nodo nodoActualizado) {
        Nodo nodo = obtenerPorId(id);
        
        if (nodoActualizado.getNombre() != null) {
            nodo.setNombre(nodoActualizado.getNombre());
        }
        if (nodoActualizado.getEstado() != null) {
            nodo.setEstado(nodoActualizado.getEstado());
        }
        if (nodoActualizado.getProbabilidadPropagacion() != null) {
            nodo.setProbabilidadPropagacion(nodoActualizado.getProbabilidadPropagacion());
        }
        if (nodoActualizado.getUmbral() != null) {
            nodo.setUmbral(nodoActualizado.getUmbral());
        }
        
        return nodoRepository.save(nodo);
    }

    /**
     * Obtiene los nodos de un grafo ordenados por centralidad de grado (descendente).
     * @param grafoId ID del grafo
     * @return Lista de nodos ordenada por centralidadGrado descendente
     */
    public List<Nodo> obtenerTopPorGrado(int grafoId) {
        return obtenerPorGrafo(grafoId).stream()
                .sorted((n1, n2) -> Double.compare(n2.getCentralidadGrado(), n1.getCentralidadGrado()))
                .toList();
    }

    /**
     * Obtiene los nodos de un grafo ordenados por betweenness centrality (descendente).
     * @param grafoId ID del grafo
     * @return Lista de nodos ordenada por betweenness descendente
     */
    public List<Nodo> obtenerTopPorBetweenness(int grafoId) {
        return obtenerPorGrafo(grafoId).stream()
                .sorted((n1, n2) -> Double.compare(n2.getBetweenness(), n1.getBetweenness()))
                .toList();
    }
}
