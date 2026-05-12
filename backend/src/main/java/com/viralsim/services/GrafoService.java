package com.viralsim.services;

import com.viralsim.models.Grafo;
import com.viralsim.repositories.GrafoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GrafoService {
    
    private final GrafoRepository grafoRepository;

    public GrafoService(GrafoRepository grafoRepository) {
        this.grafoRepository = grafoRepository;
    }

    public Grafo crearGrafo(int totalNodos) {
        Grafo grafo = new Grafo();
        grafo.setTotalNodos(totalNodos);
        return grafoRepository.save(grafo);
    }

    public List<Grafo> obtenerTodos() {
        return grafoRepository.findAll();
    }

    public Grafo obtenerPorId(int id) {
        return grafoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Grafo no encontrado: " + id));
    }
}
