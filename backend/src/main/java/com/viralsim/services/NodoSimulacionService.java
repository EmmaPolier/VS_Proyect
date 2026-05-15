package com.viralsim.services;

import com.viralsim.models.*;
import com.viralsim.repositories.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NodoSimulacionService {

    private final NodoSimulacionRepository nodoSimulacionRepository;
    private final SimulacionRepository simulacionRepository;
    private final PasoSimulacionRepository pasoRepository;
    private final NodoRepository nodoRepository;
    private final EstadoCatalogoRepository estadoRepository;

    public NodoSimulacionService(NodoSimulacionRepository nodoSimulacionRepository,
            SimulacionRepository simulacionRepository,
            PasoSimulacionRepository pasoRepository,
            NodoRepository nodoRepository,
            EstadoCatalogoRepository estadoRepository) {
        this.nodoSimulacionRepository = nodoSimulacionRepository;
        this.simulacionRepository = simulacionRepository;
        this.pasoRepository = pasoRepository;
        this.nodoRepository = nodoRepository;
        this.estadoRepository = estadoRepository;
    }

    public NodoSimulacion crear(int simulacionId, int pasoId, int nodoId, int estadoId, Integer pasoInfeccion) {
        Simulacion simulacion = simulacionRepository.findById(simulacionId)
                .orElseThrow(() -> new RuntimeException("Simulacion no encontrada: " + simulacionId));
        PasoSimulacion paso = pasoRepository.findById(pasoId)
                .orElseThrow(() -> new RuntimeException("Paso no encontrado: " + pasoId));
        Nodo nodo = nodoRepository.findById(nodoId)
                .orElseThrow(() -> new RuntimeException("Nodo no encontrado: " + nodoId));
        EstadoCatalogo estado = estadoRepository.findById(estadoId)
                .orElseThrow(() -> new RuntimeException("Estado no encontrado: " + estadoId));

        NodoSimulacion ns = new NodoSimulacion();
        ns.setSimulacion(simulacion);
        ns.setPasoSimulacion(paso);
        ns.setNodo(nodo);
        ns.setEstado(estado);
        ns.setPasoInfeccion(pasoInfeccion);
        return nodoSimulacionRepository.save(ns);
    }

    public List<NodoSimulacion> obtenerPorPaso(int pasoId) {
        return nodoSimulacionRepository.findAll().stream()
                .filter(ns -> ns.getPasoSimulacion().getId().equals(pasoId))
                .toList();
    }
}
