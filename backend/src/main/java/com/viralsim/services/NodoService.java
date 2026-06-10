package com.viralsim.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.viralsim.dto.NodoUpdateDTO;
import com.viralsim.models.EstadoCatalogo;
import com.viralsim.models.Grafo;
import com.viralsim.models.Nodo;
import com.viralsim.repositories.EstadoCatalogoRepository;
import com.viralsim.repositories.GrafoRepository;
import com.viralsim.repositories.NodoRepository;

@Service
public class NodoService {

    private final NodoRepository nodoRepository;
    private final GrafoRepository grafoRepository;
    private final EstadoCatalogoRepository estadoRepository;

    public NodoService(NodoRepository nodoRepository, GrafoRepository grafoRepository,
            EstadoCatalogoRepository estadoRepository) {
        this.nodoRepository = nodoRepository;
        this.grafoRepository = grafoRepository;
        this.estadoRepository = estadoRepository;
    }

    public Nodo crearNodo(Integer grafoId, String nombre) {
        Grafo grafo = grafoRepository.findById(grafoId)
                .orElseThrow(() -> new RuntimeException("Grafo no encontrado: " + grafoId));
        EstadoCatalogo estadoInicial = estadoRepository.findById(0)
                .orElseThrow(() -> new RuntimeException("Estado NO_INFORMADO no esncontrado"));

        Nodo nodo = new Nodo();
        nodo.setGrafo(grafo);
        nodo.setNombre(nombre);
        nodo.setEstado(estadoInicial);
        return nodoRepository.save(nodo);
    }

    public List<Nodo> obtenerPorGrafo(Integer grafoId) {
        return nodoRepository.findByGrafo_Id(grafoId);
    }

    public Nodo obtenerPorId(Integer id) {
        if (id == null) {
            throw new RuntimeException("El ID del nodo no puede ser nulo");
        }
        return nodoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nodo no encontrado: " + id));
    }

    /**
     * Actualiza un nodo existente con los nuevos valores.
     * 
     * @param id              ID del nodo
     * @param nodoActualizado Objeto con los campos a actualizar
     * @return El nodo actualizado
     */
  public Nodo actualizarNodo(Integer id, NodoUpdateDTO nodoActualizado) {
    Nodo nodo = obtenerPorId(id);

    if (nodoActualizado.nombre() != null) {
        nodo.setNombre(nodoActualizado.nombre());
    }
    if (nodoActualizado.probabilidadPropagacion() != null) {
        nodo.setProbabilidadPropagacion(nodoActualizado.probabilidadPropagacion());
    } else if (nodoActualizado.probabilidad() != null) {
        nodo.setProbabilidadPropagacion(nodoActualizado.probabilidad());
    }
    if (nodoActualizado.umbral() != null) {
        nodo.setUmbral(nodoActualizado.umbral());
    }
    if (nodoActualizado.estado() != null) {
        EstadoCatalogo estado = parseEstado(nodoActualizado.estado());
        if (estado != null) {
            nodo.setEstado(estado);
        }
    } else if (nodoActualizado.estadoId() != null) {
    Integer estadoId = nodoActualizado.estadoId();
    if (estadoId != null) {  // doble check para satisfacer @NonNull
        estadoRepository.findById(estadoId)
                .ifPresent(nodo::setEstado);
    }
}
    return nodoRepository.save(nodo);
}



private EstadoCatalogo parseEstado(String estado) {
        if (estado == null) {
            return null;
        }
        String clave = estado.trim().toLowerCase();
        Integer estadoId = switch (clave) {
            case "active", "activo" -> 1;
            case "passive", "pasivo" -> 2;
            case "resistant", "resistente" -> 3;
            case "uninformed", "no informado" -> 0;
            default -> null;
        };
        if (estadoId == null) {
            return null;
        }
        return estadoRepository.findById(estadoId).orElse(null);
    }

    /**
     * Obtiene los nodos de un grafo ordenados por centralidad de grado
     * (descendente).
     * 
     * @param grafoId ID del grafo
     * @return Lista de nodos ordenada por centralidadGrado descendente
     */
    public List<Nodo> obtenerTopPorGrado(Integer grafoId) {
        return obtenerPorGrafo(grafoId).stream()
                .sorted((n1, n2) -> Double.compare(n2.getCentralidadGrado(), n1.getCentralidadGrado()))
                .toList();
    }

    /**
     * Obtiene los nodos de un grafo ordenados por betweenness centrality
     * (descendente).
     * 
     * @param grafoId ID del grafo
     * @return Lista de nodos ordenada por betweenness descendente
     */
    public List<Nodo> obtenerTopPorBetweenness(Integer grafoId) {
        return obtenerPorGrafo(grafoId).stream()
                .sorted((n1, n2) -> Double.compare(n2.getBetweenness(), n1.getBetweenness()))
                .toList();
    }
}
