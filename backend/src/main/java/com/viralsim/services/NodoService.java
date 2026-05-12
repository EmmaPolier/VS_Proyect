package com.viralsim.services;

import com.viralsim.models.EstadoCatalogo;
import com.viralsim.models.Grafo;
import com.viralsim.models.Nodo;
import com.viralsim.repositories.EstadoCatalogoRepository;
import com.viralsim.repositories.GrafoRepository;
import com.viralsim.repositories.NodoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
