package com.viralsim.services;

import com.viralsim.models.Grafo;
import com.viralsim.models.ModeloPropagacion;
import com.viralsim.models.Nodo;
import com.viralsim.models.Simulacion;
import com.viralsim.models.PasoSimulacion;
import com.viralsim.models.NodoSimulacion;
import com.viralsim.repositories.GrafoRepository;
import com.viralsim.repositories.ModeloPropagacionRepository;
import com.viralsim.repositories.NodoRepository;
import com.viralsim.repositories.SimulacionRepository;
import com.viralsim.repositories.PasoSimulacionRepository;
import com.viralsim.repositories.NodoSimulacionRepository;
import com.viralsim.dto.MetricasSimulacion;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SimulacionService {

    private final SimulacionRepository simulacionRepository;
    private final GrafoRepository grafoRepository;
    private final ModeloPropagacionRepository modeloRepository;
    private final NodoRepository nodoRepository;
    private final PasoSimulacionRepository pasoSimulacionRepository;
    private final NodoSimulacionRepository nodoSimulacionRepository;

    public SimulacionService(SimulacionRepository simulacionRepository,
            GrafoRepository grafoRepository,
            ModeloPropagacionRepository modeloRepository,
            NodoRepository nodoRepository,
            PasoSimulacionRepository pasoSimulacionRepository,
            NodoSimulacionRepository nodoSimulacionRepository) {
        this.simulacionRepository = simulacionRepository;
        this.grafoRepository = grafoRepository;
        this.modeloRepository = modeloRepository;
        this.nodoRepository = nodoRepository;
        this.pasoSimulacionRepository = pasoSimulacionRepository;
        this.nodoSimulacionRepository = nodoSimulacionRepository;
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

    /**
     * Obtiene las métricas de una simulación.
     * @param simulacionId ID de la simulación
     * @return DTO con las métricas principales
     */
    public MetricasSimulacion obtenerMetricas(int simulacionId) {
        Simulacion sim = obtenerPorId(simulacionId);
        
        // Calcular alcance porcentaje
        double alcance = (sim.getTotalInformados() / 250.0) * 100.0;
        
        return new MetricasSimulacion(
            sim.getId(),
            sim.getGrafo().getId(),
            sim.getModelo().getId(),
            sim.getNodoSemilla().getId(),
            sim.getIniciadaEn(),
            sim.getTotalPasos(),
            sim.getTotalInformados(),
            sim.getPaso50Porciento(),
            sim.getResultado(),
            alcance
        );
    }

    /**
     * Obtiene todos los pasos de una simulación.
     * @param simulacionId ID de la simulación
     * @return Lista de PasoSimulacion ordenados por numeroPaso
     */
    public List<PasoSimulacion> obtenerPasos(int simulacionId) {
        return pasoSimulacionRepository.findAll().stream()
                .filter(p -> p.getSimulacion().getId().equals(simulacionId))
                .sorted((p1, p2) -> Integer.compare(p1.getNumeroPaso(), p2.getNumeroPaso()))
                .toList();
    }

    /**
     * Obtiene los nodos simulación de un paso específico.
     * @param simulacionId ID de la simulación
     * @param paso Número del paso
     * @return Lista de NodoSimulacion del paso
     */
    public List<NodoSimulacion> obtenerNodoSimulacionPorPaso(int simulacionId, int paso) {
        return nodoSimulacionRepository.findAll().stream()
                .filter(ns -> ns.getSimulacion().getId().equals(simulacionId) && 
                        ns.getPasoSimulacion().getNumeroPaso().equals(paso))
                .toList();
    }
}
