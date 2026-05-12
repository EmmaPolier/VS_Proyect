package com.viralsim.services;

import com.viralsim.models.PasoSimulacion;
import com.viralsim.models.Simulacion;
import com.viralsim.repositories.PasoSimulacionRepository;
import com.viralsim.repositories.SimulacionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PasoSimulacionService {

    private final PasoSimulacionRepository pasoRepository;
    private final SimulacionRepository simulacionRepository;

    public PasoSimulacionService(PasoSimulacionRepository pasoRepository,
            SimulacionRepository simulacionRepository) {
        this.pasoRepository = pasoRepository;
        this.simulacionRepository = simulacionRepository;
    }

    public PasoSimulacion crear(int simulacionId, int numeroPaso,
            int nuevosInformados, int totalActivos, int totalResistentes) {
        Simulacion simulacion = simulacionRepository.findById(simulacionId)
                .orElseThrow(() -> new RuntimeException("Simulacion no encontrada: " + simulacionId));

        PasoSimulacion paso = new PasoSimulacion();
        paso.setSimulacion(simulacion);
        paso.setNumeroPaso(numeroPaso);
        paso.setNuevosInformados(nuevosInformados);
        paso.setTotalActivos(totalActivos);
        paso.setTotalResistentes(totalResistentes);
        return pasoRepository.save(paso);
    }

    public List<PasoSimulacion> obtenerPorSimulacion(int simulacionId) {
        return pasoRepository.findAll().stream()
                .filter(p -> p.getSimulacion().getId().equals(simulacionId))
                .toList();
    }

    public PasoSimulacion obtenerPorId(int id) {
        return pasoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paso no encontrado: " + id));
    }
}
