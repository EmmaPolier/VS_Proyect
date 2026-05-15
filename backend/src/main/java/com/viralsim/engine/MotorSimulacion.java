package com.viralsim.engine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.viralsim.models.*;
import com.viralsim.repositories.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MotorSimulacion {

    @Autowired
    private SimulacionRepository simulacionRepository;
    @Autowired
    private NodoRepository nodoRepository;
    @Autowired
    private AristaRepository aristaRepository;
    @Autowired
    private PasoSimulacionRepository pasoSimulacionRepository;
    @Autowired
    private NodoSimulacionRepository nodoSimulacionRepository;
    @Autowired
    private EstadoCatalogoRepository estadoCatalogoRepository;
    @Autowired
    private GrafoRepository grafoRepository;
    @Autowired
    private ModeloPropagacionRepository modeloPropagacionRepository;

    public ResultadoSimulacion ejecutar(int simulacionId) {
        Simulacion simulacion = simulacionRepository.findById(simulacionId)
                .orElseThrow(() -> new RuntimeException("Simulación no encontrada" + simulacionId));

        Integer grafoId = simulacion.getGrafo().getId();
        Integer modeloId = simulacion.getModelo().getId();
        Integer nodoSemillaId = simulacion.getNodoSemilla().getId();

        List<Nodo> nodos = nodoRepository.findByGrafo_Id(grafoId);
        Map<Integer, Nodo> nodosPorId = nodos.stream()
                .collect(Collectors.toMap(Nodo::getId, n -> n));

        List<Arista> aristas = aristaRepository.findByGrafo_Id(grafoId);

        Map<Integer, List<Nodo>> adyacencia = new HashMap<>();
        for (Nodo nodo : nodos) {
            adyacencia.put(nodo.getId(), new ArrayList<>());
        }

        Map<String, Arista> aristasPorPar = new HashMap<>();
        for (Arista arista : aristas) {
            Integer origen = arista.getNodoOrigen().getId();
            Integer destino = arista.getNodoDestino().getId();

            adyacencia.get(origen).add(arista.getNodoDestino());
            adyacencia.get(destino).add(arista.getNodoOrigen());

            Integer min = Math.min(origen, destino);
            Integer max = Math.max(origen, destino);
            aristasPorPar.put(min + "-" + max, arista);
        }

        EstrategiaPropagacion estrategia;
        switch (modeloId) {
            case 1:
                estrategia = new ModeloViral();
                break;
            case 2:
                estrategia = new ModeloCascada();
                break;
            case 3:
                estrategia = new ModeloThreshold();
                break;
            default:
                throw new RuntimeException("Modelo no soportado: " + modeloId);
        }

        Nodo nodoSemilla = nodosPorId.get(nodoSemillaId);
        EstadoCatalogo estadoActivo = estadoCatalogoRepository.findById(1)
                .orElseThrow(() -> new RuntimeException("Estado INFORMADO_ACTIVO no encontrado"));
        nodoSemilla.setEstado(estadoActivo);

        List<Nodo> nodosActivos = new ArrayList<>();
        nodosActivos.add(nodoSemilla);
        int paso = 1;
        Integer paso50 = null;

        nodoRepository.save(nodoSemilla);

        while (!nodosActivos.isEmpty()) {
            List<Nodo> nuevosInformados = estrategia.propagar(nodosActivos, nodosPorId, adyacencia, aristasPorPar);

            if (nuevosInformados.isEmpty()) {
                break; // No hay más nodos para informar, terminamos la simulación
            }

            for (Nodo nodo : nuevosInformados) {
                nodo.setEstado(estadoActivo);
                nodoRepository.save(nodo);
            }

            int totalActivos = (int) nodos.stream().filter(n -> n.getEstado().getId() == 1).count();
            int totalPasivos = (int) nodos.stream().filter(n -> n.getEstado().getId() == 2).count();
            int totalResistentes = (int) nodos.stream().filter(n -> n.getEstado().getId() == 3).count();
            int totalInformados = totalActivos + totalPasivos;
            int totalNoInformados = 250 - totalInformados - totalResistentes;

            PasoSimulacion pasoSim = new PasoSimulacion();
            pasoSim.setSimulacion(simulacion);
            pasoSim.setNumeroPaso(paso);
            pasoSim.setNuevosInformados(nuevosInformados.size());
            pasoSim.setTotalActivos(totalActivos);
            pasoSim.setTotalPasivos(totalPasivos);
            pasoSim.setTotalResistentes(totalResistentes);
            pasoSim.setTotalInformados(totalInformados);
            PasoSimulacion pasoGuardado = pasoSimulacionRepository.save(pasoSim);

            for (Nodo nodo : nuevosInformados) {
                NodoSimulacion nodoSim = new NodoSimulacion();
                nodoSim.setSimulacion(simulacion);
                nodoSim.setPasoSimulacion(pasoGuardado);
                nodoSim.setNodo(nodo);
                nodoSim.setEstado(nodo.getEstado());
                nodoSim.setPasoInfeccion(paso);
                nodoSimulacionRepository.save(nodoSim);
            }

            double alcance = (double) totalInformados / 250;

            if (alcance >= 0.5 && paso50 == null) {
                paso50 = paso;
            }

            nodosActivos = nuevosInformados.stream()
                    .filter(n -> n.getEstado().getId() == 1)
                    .collect(Collectors.toList());

            paso++;
        }

        int totalActivos = (int) nodos.stream().filter(n -> n.getEstado().getId() == 1).count();
        int totalPasivos = (int) nodos.stream().filter(n -> n.getEstado().getId() == 2).count();
        int totalResistentes = (int) nodos.stream().filter(n -> n.getEstado().getId() == 3).count();
        int totalInformados = totalActivos + totalPasivos;
        int totalNoInformados = 250 - totalInformados - totalResistentes;
        double alcanceFinal = (double) totalInformados / 250;

        simulacion.setTotalPasos(paso - 1);
        simulacion.setTotalInformados(totalInformados);
        simulacion.setPaso50Porciento(paso50);
        simulacion.setResultado("COMPLETADA");
        simulacionRepository.save(simulacion);

        return new ResultadoSimulacion(
                simulacionId,
                paso - 1,
                totalInformados,
                alcanceFinal * 100,
                paso50,
                totalActivos,
                totalPasivos,
                totalResistentes,
                totalNoInformados);
    }

    /**
     * Compara los 3 modelos de propagación sobre el mismo grafo y nodo semilla.
     * 
     * @param grafoId       ID del grafo a usar
     * @param nodoSemillaId ID del nodo inicial
     * @return Lista de 3 ResultadoSimulacion (Viral, Cascada, Threshold)
     */
    public List<ResultadoSimulacion> compararModelos(int grafoId, int nodoSemillaId) {
        List<ResultadoSimulacion> resultados = new ArrayList<>();

        // Verificar que el grafo y nodo existen
        Grafo grafo = grafoRepository.findById(grafoId)
                .orElseThrow(() -> new RuntimeException("Grafo no encontrado: " + grafoId));
        Nodo nodoSemilla = nodoRepository.findById(nodoSemillaId)
                .orElseThrow(() -> new RuntimeException("Nodo semilla no encontrado: " + nodoSemillaId));

        // Ejecutar cada modelo (1=Viral, 2=Cascada, 3=Threshold)
        for (int loopModeloId = 1; loopModeloId <= 3; loopModeloId++) {
            int modeloId = loopModeloId;  // Capturar valor para el catch block
            try {
                // **RESETEAR ESTADOS** de todos los nodos del grafo
                List<Nodo> nodosGrafo = nodoRepository.findByGrafo_Id(grafoId);
                EstadoCatalogo estadoNoInformado = estadoCatalogoRepository.findById(0)
                    .orElseThrow(() -> new RuntimeException("Estado NO_INFORMADO no encontrado"));
                
                for (Nodo nodo : nodosGrafo) {
                    // Solo resetear si no es RESISTENTE (estado 3)
                    if (nodo.getEstado().getId() != 3) {
                        nodo.setEstado(estadoNoInformado);
                        nodoRepository.save(nodo);
                    }
                }
                // Crear simulación para este modelo
                Simulacion simulacion = new Simulacion();
                simulacion.setGrafo(grafo);

                ModeloPropagacion modelo = modeloPropagacionRepository.findById(modeloId)
                        .orElseThrow(() -> new RuntimeException("Modelo no encontrado: " + modeloId));
                simulacion.setModelo(modelo);
                simulacion.setNodoSemilla(nodoSemilla);

                // Guardar y ejecutar
                Simulacion simGuardada = simulacionRepository.save(simulacion);
                ResultadoSimulacion resultado = ejecutar(simGuardada.getId());
                resultados.add(resultado);

            } catch (Exception e) {
                throw new RuntimeException("Error al comparar modelo " + modeloId + ": " + e.getMessage());
            }
        }
        return resultados;
    }

    public record ResultadoSimulacion(
            int simulacionId,
            int totalPasos,
            int totalInformados,
            double alcancePorcentaje,
            Integer paso50Porciento,
            int totalActivos,
            int totalPasivos,
            int totalResistentes,
            int totalNoInformados) {
    }
}
