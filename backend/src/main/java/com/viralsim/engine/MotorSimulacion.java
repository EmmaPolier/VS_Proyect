package com.viralsim.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.viralsim.models.Arista;
import com.viralsim.models.EstadoCatalogo;
import com.viralsim.models.Grafo;
import com.viralsim.models.ModeloPropagacion;
import com.viralsim.models.Nodo;
import com.viralsim.models.NodoSimulacion;
import com.viralsim.models.PasoSimulacion;
import com.viralsim.models.Simulacion;
import com.viralsim.repositories.AristaRepository;
import com.viralsim.repositories.EstadoCatalogoRepository;
import com.viralsim.repositories.GrafoRepository;
import com.viralsim.repositories.ModeloPropagacionRepository;
import com.viralsim.repositories.NodoRepository;
import com.viralsim.repositories.NodoSimulacionRepository;
import com.viralsim.repositories.PasoSimulacionRepository;
import com.viralsim.repositories.SimulacionRepository;

@Service
public class MotorSimulacion {

    private static final Logger logger = LoggerFactory.getLogger(MotorSimulacion.class);

    @Autowired private SimulacionRepository simulacionRepository;
    @Autowired private NodoRepository nodoRepository;
    @Autowired private AristaRepository aristaRepository;
    @Autowired private PasoSimulacionRepository pasoSimulacionRepository;
    @Autowired private NodoSimulacionRepository nodoSimulacionRepository;
    @Autowired private EstadoCatalogoRepository estadoCatalogoRepository;
    @Autowired private GrafoRepository grafoRepository;
    @Autowired private ModeloPropagacionRepository modeloPropagacionRepository;

    public ResultadoSimulacion ejecutar(int simulacionId) {
        Simulacion simulacion = simulacionRepository.findById(simulacionId)
                .orElseThrow(() -> new RuntimeException("Simulación no encontrada: " + simulacionId));

        Integer grafoId      = simulacion.getGrafo().getId();
        Integer modeloId     = simulacion.getModelo().getId();
        Integer nodoSemillaId = simulacion.getNodoSemilla().getId();

        logger.info("🚀 Iniciando simulación [ID: {}] grafo={} modelo={} semilla={}",
                simulacionId, grafoId, modeloId, nodoSemillaId);

        // ── Cargar nodos y aristas ────────────────────────────────────────────
        List<Nodo> nodos = nodoRepository.findByGrafo_Id(grafoId);
        Map<Integer, Nodo> nodosPorId = nodos.stream()
                .collect(Collectors.toMap(Nodo::getId, n -> n));

        List<Arista> aristas = aristaRepository.findByGrafo_Id(grafoId);

        Map<Integer, List<Nodo>> adyacencia = new HashMap<>();
        Map<String, Arista> aristasPorPar   = new HashMap<>();
        for (Nodo nodo : nodos) adyacencia.put(nodo.getId(), new ArrayList<>());
        for (Arista arista : aristas) {
            Integer origen  = arista.getNodoOrigen().getId();
            Integer destino = arista.getNodoDestino().getId();
            adyacencia.get(origen).add(arista.getNodoDestino());
            adyacencia.get(destino).add(arista.getNodoOrigen());
            Integer min = Math.min(origen, destino);
            Integer max = Math.max(origen, destino);
            aristasPorPar.put(min + "-" + max, arista);
        }

        // ── Estados del catálogo ──────────────────────────────────────────────
        EstadoCatalogo estadoNoInformado = estadoCatalogoRepository.findById(0)
                .orElseThrow(() -> new RuntimeException("Estado NO_INFORMADO no encontrado"));
        EstadoCatalogo estadoActivo      = estadoCatalogoRepository.findById(1)
                .orElseThrow(() -> new RuntimeException("Estado ACTIVO no encontrado"));
        EstadoCatalogo estadoPasivo      = estadoCatalogoRepository.findById(2)
                .orElseThrow(() -> new RuntimeException("Estado PASIVO no encontrado"));
        EstadoCatalogo estadoResistente  = estadoCatalogoRepository.findById(3)
                .orElseThrow(() -> new RuntimeException("Estado RESISTENTE no encontrado"));

        // ── Estrategia de propagación ─────────────────────────────────────────
        EstrategiaPropagacion estrategia = switch (modeloId) {
            case 1  -> new ModeloViral();
            case 2  -> new ModeloCascada();
            case 3  -> new ModeloThreshold();
            default -> throw new RuntimeException("Modelo no soportado: " + modeloId);
        };

        // ── Reiniciar estados (respetar resistentes) ──────────────────────────
        for (Nodo nodo : nodos) {
            if (nodo.getEstado().getId() != 3) {
                nodo.setEstado(estadoNoInformado);
            }
        }
        nodoRepository.saveAll(nodos);

        // ── Nodo semilla → activo ─────────────────────────────────────────────
        Nodo nodoSemilla = nodosPorId.get(nodoSemillaId);
        nodoSemilla.setEstado(estadoActivo);
        nodoRepository.save(nodoSemilla);

        List<Nodo> nodosActivos = new ArrayList<>();
        nodosActivos.add(nodoSemilla);

        int paso = 1;
        int ultimoPasoGuardado = 0;
        Integer paso50 = null;

        // ── Loop principal ────────────────────────────────────────────────────
        while (!nodosActivos.isEmpty()) {

            // 1. Propagar: obtener nuevos infectados en este paso
            List<Nodo> nuevosInformados = estrategia.propagar(
                    nodosActivos, nodosPorId, adyacencia, aristasPorPar);

            // 2. Transición active → passive para los nodos que ya propagaron
            for (Nodo nodoActivo : nodosActivos) {
                nodoActivo.setEstado(estadoPasivo);
                nodoRepository.save(nodoActivo);
            }

            // 3. Marcar nuevos como activos
            for (Nodo nodo : nuevosInformados) {
                nodo.setEstado(estadoActivo);
                nodoRepository.save(nodo);
            }

            // 4. Contadores con estados actualizados
            int totalActivos     = (int) nodos.stream().filter(n -> n.getEstado().getId() == 1).count();
            int totalPasivos     = (int) nodos.stream().filter(n -> n.getEstado().getId() == 2).count();
            int totalResistentes = (int) nodos.stream().filter(n -> n.getEstado().getId() == 3).count();
            int totalInformados  = totalActivos + totalPasivos;
            int totalNodos       = nodos.size();

            logger.info("📊 Paso {}: activos={} pasivos={} resistentes={} informados={}/{}",
                    paso, totalActivos, totalPasivos, totalResistentes, totalInformados, totalNodos);

            // 5. Guardar PasoSimulacion
            PasoSimulacion pasoSim = new PasoSimulacion();
            pasoSim.setSimulacion(simulacion);
            pasoSim.setNumeroPaso(paso);
            pasoSim.setNuevosInformados(nuevosInformados.size());
            pasoSim.setTotalActivos(totalActivos);
            pasoSim.setTotalPasivos(totalPasivos);
            pasoSim.setTotalResistentes(totalResistentes);
            pasoSim.setTotalInformados(totalInformados);
            PasoSimulacion pasoGuardado = pasoSimulacionRepository.save(pasoSim);
            ultimoPasoGuardado = paso;

            // 6. Guardar NodoSimulacion para TODOS los nodos en este paso
            //    (el frontend necesita el estado completo del grafo por paso)
            for (Nodo nodo : nodos) {
                NodoSimulacion ns = new NodoSimulacion();
                ns.setSimulacion(simulacion);
                ns.setPasoSimulacion(pasoGuardado);
                ns.setNodo(nodo);
                ns.setEstado(nodo.getEstado());
                // pasoInfeccion: el paso en que se infectó por primera vez
                if (nuevosInformados.contains(nodo)) {
                    ns.setPasoInfeccion(paso);
                } else if (nodo.getId().equals(nodoSemillaId) && paso == 1) {
                    ns.setPasoInfeccion(1);
                }
                nodoSimulacionRepository.save(ns);
            }

            // 7. Verificar paso 50%
            double alcance = (double) totalInformados / totalNodos;
            if (alcance >= 0.5 && paso50 == null) paso50 = paso;

            // 8. Próxima iteración: solo los nuevos activos propagan
            nodosActivos = nuevosInformados.stream()
                    .filter(n -> n.getEstado().getId() == 1)
                    .collect(Collectors.toList());

            paso++;

            // Parada de seguridad
            if (paso > totalNodos + 10) {
                logger.warn("⚠️ Parada de seguridad en paso {}", paso);
                break;
            }
        }

        // ── Contadores finales ────────────────────────────────────────────────
        int totalActivos     = (int) nodos.stream().filter(n -> n.getEstado().getId() == 1).count();
        int totalPasivos     = (int) nodos.stream().filter(n -> n.getEstado().getId() == 2).count();
        int totalResistentes = (int) nodos.stream().filter(n -> n.getEstado().getId() == 3).count();
        int totalInformados  = totalActivos + totalPasivos;
        int totalNoInformados = nodos.size() - totalInformados - totalResistentes;
        double alcanceFinal  = (double) totalInformados / nodos.size();

        simulacion.setTotalPasos(ultimoPasoGuardado);
        simulacion.setTotalInformados(totalInformados);
        simulacion.setPaso50Porciento(paso50);
        simulacion.setResultado("COMPLETADA");
        simulacionRepository.save(simulacion);

        logger.info("✨ Simulación {} completada: {} pasos, {} informados ({:.1f}%)",
                simulacionId, ultimoPasoGuardado, totalInformados, alcanceFinal * 100);

        return new ResultadoSimulacion(
                simulacionId,
                ultimoPasoGuardado,
                totalInformados,
                alcanceFinal * 100,
                paso50,
                totalActivos,
                totalPasivos,
                totalResistentes,
                totalNoInformados);
    }

    // ── Comparar 3 modelos ────────────────────────────────────────────────────
    public List<ResultadoSimulacion> compararModelos(int grafoId, int nodoSemillaId) {
        List<ResultadoSimulacion> resultados = new ArrayList<>();

        Grafo grafo = grafoRepository.findById(grafoId)
                .orElseThrow(() -> new RuntimeException("Grafo no encontrado: " + grafoId));
        Nodo nodoSemilla = nodoRepository.findById(nodoSemillaId)
                .orElseThrow(() -> new RuntimeException("Nodo semilla no encontrado: " + nodoSemillaId));

        EstadoCatalogo estadoNoInformado = estadoCatalogoRepository.findById(0)
                .orElseThrow(() -> new RuntimeException("Estado NO_INFORMADO no encontrado"));

        for (int loopModeloId = 1; loopModeloId <= 3; loopModeloId++) {
            int modeloId = loopModeloId;
            try {
                // Resetear estados (respetar resistentes)
                List<Nodo> nodosGrafo = nodoRepository.findByGrafo_Id(grafoId);
                for (Nodo nodo : nodosGrafo) {
                    if (nodo.getEstado().getId() != 3) {
                        nodo.setEstado(estadoNoInformado);
                    }
                }
                nodoRepository.saveAll(nodosGrafo);

                ModeloPropagacion modelo = modeloPropagacionRepository.findById(modeloId)
                        .orElseThrow(() -> new RuntimeException("Modelo no encontrado: " + modeloId));

                Simulacion simulacion = new Simulacion();
                simulacion.setGrafo(grafo);
                simulacion.setModelo(modelo);
                simulacion.setNodoSemilla(nodoSemilla);

                Simulacion simGuardada = simulacionRepository.save(simulacion);
                ResultadoSimulacion resultado = ejecutar(simGuardada.getId());
                resultados.add(resultado);

            } catch (Exception e) {
                throw new RuntimeException("Error comparando modelo " + modeloId + ": " + e.getMessage());
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
            int totalNoInformados) {}
}