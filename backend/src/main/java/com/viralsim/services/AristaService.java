package com.viralsim.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.viralsim.models.Arista;
import com.viralsim.models.Nodo;
import com.viralsim.repositories.AristaRepository;
import com.viralsim.repositories.NodoRepository;

@Service
public class AristaService {

    private final AristaRepository aristaRepository;
    private final NodoRepository nodoRepository;

    public AristaService(AristaRepository aristaRepository,
            NodoRepository nodoRepository) {
        this.aristaRepository = aristaRepository;
        this.nodoRepository = nodoRepository;
    }

    public Arista crearArista(int nodoOrigenId, int nodoDestinoId) {
        Nodo origen = nodoRepository.findById(nodoOrigenId)
                .orElseThrow(() -> new RuntimeException("Nodo origen no encontrado: " + nodoOrigenId));
        Nodo destino = nodoRepository.findById(nodoDestinoId)
                .orElseThrow(() -> new RuntimeException("Nodo destino no encontrado: " + nodoDestinoId));

        Arista arista = new Arista();
        arista.setNodoOrigen(origen);
        arista.setNodoDestino(destino);
        return aristaRepository.save(arista);
    }

    public List<Arista> obtenerTodas() {
        return aristaRepository.findAll();
    }

    public Arista obtenerPorId(int id) {
        return aristaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Arista no encontrada: " + id));
    }

    /**
     * Obtiene todas las aristas de un grafo específico.
     * @param grafoId ID del grafo
     * @return Lista de aristas del grafo
     */
    public List<Arista> obtenerPorGrafo(int grafoId) {
        return aristaRepository.findAll().stream()
                .filter(a -> a.getNodoOrigen().getGrafo().getId().equals(grafoId))
                .toList();
    }
}