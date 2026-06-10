package com.viralsim.engine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.viralsim.models.Arista;
import com.viralsim.models.Nodo;

public class ModeloThreshold implements EstrategiaPropagacion {

    @Override
    public List<Nodo> propagar(
            List<Nodo> nodosActivos,
            Map<Integer, Nodo> nodosPorId,
            Map<Integer, List<Nodo>> adyacencia,
            Map<String, Arista> aristasPorPar) {

        List<Nodo> nuevosInformados = new ArrayList<>();
        Set<Integer> idsNuevos = new HashSet<>();

        for (Nodo nodo : nodosPorId.values()) {
            if (nodo.getEstado().getId() != 0) {
                continue; // Solo los nodos susceptibles pueden ser informados
            }

            List<Nodo> vecinosNodo = adyacencia.getOrDefault(nodo.getId(), List.of());
            int totalVecinos = vecinosNodo.size();
            if (totalVecinos > 0) {
                int vecinosInformados = 0;
                for (Nodo vecino : vecinosNodo) {
                    if (vecino.getEstado().getId() != 0) {
                        vecinosInformados++;
                    }
                }

                double fraccion = (double) vecinosInformados / totalVecinos;
                double umbral = nodo.getUmbral();
                if (umbral > 1) {
                    umbral /= 100.0;
                }

                if (fraccion >= umbral && !idsNuevos.contains(nodo.getId())) {
                    idsNuevos.add(nodo.getId());
                    nuevosInformados.add(nodo);
                }
            }
        }
        return nuevosInformados;
    }
    
}
