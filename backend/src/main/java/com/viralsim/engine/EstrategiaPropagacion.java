package com.viralsim.engine;

import java.util.List;
import java.util.Map;
import com.viralsim.models.Arista;
import com.viralsim.models.Nodo;

public interface EstrategiaPropagacion {
    List<Nodo> propagar(
        List<Nodo> nodosActivos,
        Map<Integer, Nodo> nodosPorId,
        Map<Integer, List<Nodo>> adyacencia,
        Map<String, Arista> aristasPorPar);
}
