package com.viralsim.services;

import com.viralsim.models.Arista;
import com.viralsim.models.Nodo;
import com.viralsim.repositories.AristaRepository;
import com.viralsim.repositories.NodoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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
}