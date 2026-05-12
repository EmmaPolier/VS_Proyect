package com.viralsim.services;

import com.viralsim.models.Grafo;
import com.viralsim.models.ModeloPropagacion;
import com.viralsim.models.Nodo;
import com.viralsim.models.Simulacion;
import com.viralsim.repositories.GrafoRepository;
import com.viralsim.repositories.ModeloPropagacionRepository;
import com.viralsim.repositories.NodoRepository;
import com.viralsim.repositories.SimulacionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SimulacionService {

    private final SimulacionRepository simulacionRepository;
    private final GrafoRepository grafoRepository;
    private final ModeloPropagacionRepository modeloRepository;
    private final NodoRepository nodoRepository;

    public SimulacionService(SimulacionRepository simulacionRepository,
            GrafoRepository grafoRepository,
            ModeloPropagacionRepository modeloRepository,
            NodoRepository nodoRepository) {
        this.simulacionRepository = simulacionRepository;
        this.grafoRepository = grafoRepository;
        this.modeloRepository = modeloRepository;
        this.nodoRepository = nodoRepository;
    }

    public Simulacion crearSimulacion(int grafoId, int modeloId, int nodoSemillaId) {
        Grafo grafo = grafoRepository.findById(grafoId)
                .orElseThrow(() -> new RuntimeException("Grafo no encontrado: " + grafoId));
        ModeloPropagacion modelo = modeloRepository.findById(modeloId)
                .orElseThrow(() -> new RuntimeException("Modelo no encontrado: " + modeloId));
        Nodo nodoSemilla = nodoRepository.findById(nodoSemillaId)
                .orElseThrow(() -> new RuntimeException("Nodo semilla no encontrado: " + nodoSemillaId));

        Simulacion simulacion = new Simulacion();
        simulacion.setGrafo(grafo);
        simulacion.setModelo(modelo);
        simulacion.setNodoSemilla(nodoSemilla);
        return simulacionRepository.save(simulacion);
    }

    public List<Simulacion> obtenerTodas() {
        return simulacionRepository.findAll();
    }

    public Simulacion obtenerPorId(int id) {
        return simulacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Simulacion no encontrada: " + id));
    }
}
