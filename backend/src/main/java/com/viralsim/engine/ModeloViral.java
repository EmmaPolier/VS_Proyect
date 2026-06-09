package com.viralsim.engine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.viralsim.models.Arista;
import com.viralsim.models.Nodo;

public class ModeloViral implements EstrategiaPropagacion {

    private final Random random = new Random();
    private static final Logger logger = LoggerFactory.getLogger(ModeloViral.class);

    @Override
    public List<Nodo> propagar(
            List<Nodo> nodosActivos,
            Map<Integer, Nodo> nodosPorId,
            Map<Integer, List<Nodo>> adyacencia,
            Map<String, Arista> aristasPorPar) {

        List<Nodo> nuevosInformados = new ArrayList<>();
        Set<Integer> idsNuevos = new HashSet<>();

        logger.debug("🔥 ModeloViral.propagar() ENTRADA: {} nodos activos", nodosActivos.size());

        for (Nodo activo : nodosActivos) {
            logger.debug("  - Nodo activo ID={}, EstadoId={}, Prob={}",
                    activo.getId(), activo.getEstado().getId(), activo.getProbabilidadPropagacion());

            if (activo.getEstado().getId() != 1) {
                logger.debug("    → Estado no es 1, saltando");
                continue; // Solo los nodos activos pueden propagar
            }

            List<Nodo> vecinos = adyacencia.getOrDefault(activo.getId(), List.of());
            logger.debug("    → Tiene {} vecinos", vecinos.size());

            for (Nodo vecino : vecinos) {
                if (vecino.getEstado().getId() != 0) {
                    logger.debug("      → Vecino ID={} estado={}, no es susceptible",
                            vecino.getId(), vecino.getEstado().getId());
                    continue; // Solo los nodos susceptibles pueden ser informados
                }
                if (idsNuevos.contains(vecino.getId())) {
                    logger.debug("      → Vecino ID={} ya fue añadido", vecino.getId());
                    continue; // Evitar duplicados
                }

                double prob = random.nextDouble();
                logger.debug("      → Vecino ID={} prob={:.2f} vs threshold={:.2f}",
                        vecino.getId(), prob, activo.getProbabilidadPropagacion());

                if (prob <= activo.getProbabilidadPropagacion()) {
                    idsNuevos.add(vecino.getId());
                    nuevosInformados.add(vecino);
                    logger.debug("        ✅ Vecino ID={} INFECTADO", vecino.getId());
                }
            }
        }

        logger.debug("🔥 ModeloViral.propagar() SALIDA: {} nuevos informados", nuevosInformados.size());
        return nuevosInformados;
    }
}
