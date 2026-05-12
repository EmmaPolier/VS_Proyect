package com.viralsim.services;

import com.viralsim.models.ConfiguracionSimulacion;
import com.viralsim.models.Simulacion;
import com.viralsim.repositories.ConfiguracionSimulacionRepository;
import com.viralsim.repositories.SimulacionRepository;
import org.springframework.stereotype.Service;

@Service
public class ConfiguracionSimulacionService {

    private final ConfiguracionSimulacionRepository configRepository;
    private final SimulacionRepository simulacionRepository;

    public ConfiguracionSimulacionService(ConfiguracionSimulacionRepository configRepository,
            SimulacionRepository simulacionRepository) {
        this.configRepository = configRepository;
        this.simulacionRepository = simulacionRepository;
    }

    public ConfiguracionSimulacion crear(int simulacionId, double probabilidadGlobal, int velocidadAnimacion) {
        Simulacion simulacion = simulacionRepository.findById(simulacionId)
                .orElseThrow(() -> new RuntimeException("Simulacion no encontrada: " + simulacionId));

        ConfiguracionSimulacion config = new ConfiguracionSimulacion();
        config.setSimulacion(simulacion);
        config.setProbabilidadGlobal(probabilidadGlobal);
        config.setVelocidadAnimacion(velocidadAnimacion);
        return configRepository.save(config);
    }

    public ConfiguracionSimulacion obtenerPorSimulacion(int simulacionId) {
        return configRepository.findAll().stream()
                .filter(c -> c.getSimulacion().getId().equals(simulacionId))
                .findFirst()
                .orElseThrow(
                        () -> new RuntimeException("Configuracion no encontrada para simulacion: " + simulacionId));
    }
}
