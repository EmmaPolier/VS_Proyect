package com.viralsim.services;

import com.viralsim.models.Arista;
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
import com.viralsim.repositories.AristaRepository;  
import com.viralsim.metrics.CalculadorMetricas;

import java.util.List;

@Service
public class SimulacionService {

    private final SimulacionRepository simulacionRepository;
    private final GrafoRepository grafoRepository;
    private final ModeloPropagacionRepository modeloRepository;
    private final NodoRepository nodoRepository;
    private final PasoSimulacionRepository pasoSimulacionRepository;
    private final NodoSimulacionRepository nodoSimulacionRepository;
    private final AristaRepository aristaRepository;
    private final CalculadorMetricas calculadorMetricas;

    public SimulacionService(SimulacionRepository simulacionRepository,
            GrafoRepository grafoRepository,
            ModeloPropagacionRepository modeloRepository,
            NodoRepository nodoRepository,
            PasoSimulacionRepository pasoSimulacionRepository,
            NodoSimulacionRepository nodoSimulacionRepository,
            AristaRepository aristaRepository,
            CalculadorMetricas calculadorMetricas) {
        this.simulacionRepository = simulacionRepository;
        this.grafoRepository = grafoRepository;
        this.modeloRepository = modeloRepository;
        this.nodoRepository = nodoRepository;
        this.pasoSimulacionRepository = pasoSimulacionRepository;
        this.nodoSimulacionRepository = nodoSimulacionRepository;
        this.aristaRepository = aristaRepository;
        this.calculadorMetricas = calculadorMetricas;
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
     * @param id ID de la simulación
     * @return DTO con las métricas principales
     */
    public MetricasSimulacion obtenerMetricas(int id) {
        Simulacion simulacion = simulacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Simulación no encontrada"));

        Grafo grafo = simulacion.getGrafo();

        List<Nodo> nodos = nodoRepository.findAll().stream()
                .filter(n -> n.getGrafo().getId().equals(grafo.getId()))
                .toList();

        List<Arista> aristas = aristaRepository.findAll().stream()
                .filter(a -> a.getNodoOrigen().getGrafo().getId().equals(grafo.getId()))
                .toList();

        // Calcular métricas
        calculadorMetricas.calcularTodasLasMetricas(grafo, nodos, aristas);

        // Total informados
        long totalInformados = nodos.stream()
                .filter(n -> n.getEstado().getNombre().equalsIgnoreCase("Informado"))
                .count();

        // Alcance en porcentaje
        double alcancePorcentaje = (double) totalInformados / nodos.size() * 100;

        // Paso en que se alcanza el 50% (simplificado)
        int paso50 = (int) Math.ceil(nodos.size() * 0.5);

        // Top nodos por centralidad
        List<Nodo> topGrado = nodos.stream()
                .sorted((n1, n2) -> Double.compare(n2.getCentralidadGrado(), n1.getCentralidadGrado()))
                .toList();

        List<Nodo> topBetweenness = nodos.stream()
                .sorted((n1, n2) -> Double.compare(n2.getBetweenness(), n1.getBetweenness()))
                .toList();

        // Construir DTO con los datos
        return new MetricasSimulacion(
                simulacion.getId(),
                grafo.getId(),
                simulacion.getModelo().getId(),
                simulacion.getNodoSemilla().getId(),
                simulacion.getIniciadaEn(),
                simulacion.getTotalPasos(),
                (int) totalInformados,
                paso50,
                "OK",
                alcancePorcentaje,
                topGrado,
                topBetweenness
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
