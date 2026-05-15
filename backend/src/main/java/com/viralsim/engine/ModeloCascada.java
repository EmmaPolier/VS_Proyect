package com.viralsim.engine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import com.viralsim.models.Arista;
import com.viralsim.models.Nodo;

public class ModeloCascada implements EstrategiaPropagacion {

    private final Random random = new Random();

    @Override
    public List<Nodo> propagar(
            List<Nodo> nodosActivos,
            Map<Integer, Nodo> nodosPorId,
            Map<Integer, List<Nodo>> adyacencia,
            Map<String, Arista> aristasPorPar) {

        List<Nodo> nuevosInformados = new ArrayList<>();
        Set<Integer> idsNuevos = new HashSet<>();

        for (Nodo activo : nodosActivos) {
            if (activo.getEstado().getId() != 1) {
                continue; // Solo los nodos activos pueden propagar
            }

            List<Nodo> vecinos = adyacencia.getOrDefault(activo.getId(), List.of());
            for (Nodo vecino : vecinos) {
                if (vecino.getEstado().getId() != 0) {
                    continue; // Solo los nodos susceptibles pueden ser informados
                }
                if (idsNuevos.contains(vecino.getId())) {
                    continue; // Ya se ha marcado para informar en esta ronda
                }

                Arista arista = obtenerArista(activo.getId(), vecino.getId(), aristasPorPar);

                if (arista != null && arista.isActiva()) {
                    double prob = random.nextDouble();
                    double probabilidadArista = arista.getProbabilidadArista() != null
                            ? arista.getProbabilidadArista()
                            : 0.5;

                    if (prob <= probabilidadArista) {
                        nuevosInformados.add(vecino);
                        idsNuevos.add(vecino.getId());
                    }
                }
            }
        }
        return nuevosInformados;
    }

    private Arista obtenerArista(Integer nodo1, Integer nodo2, Map<String, Arista> aristasPorPar) {
        Integer min = Math.min(nodo1, nodo2);
        Integer max = Math.max(nodo1, nodo2);
        String clave = min + "-" + max;
        return aristasPorPar.get(clave);
    }
}
