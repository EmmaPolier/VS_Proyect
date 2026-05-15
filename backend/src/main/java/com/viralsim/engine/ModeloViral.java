package com.viralsim.engine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import com.viralsim.models.Arista;
import com.viralsim.models.Nodo;

public class ModeloViral implements EstrategiaPropagacion {
    
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
                    continue; // Evitar duplicados
                }

                double prob = random.nextDouble();
                if (prob <= activo.getProbabilidadPropagacion()) {
                    idsNuevos.add(vecino.getId());
                    nuevosInformados.add(vecino);
                } 
            }
        }        
        return nuevosInformados;
    }
}
