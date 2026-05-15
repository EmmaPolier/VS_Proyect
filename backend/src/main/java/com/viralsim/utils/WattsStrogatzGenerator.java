package com.viralsim.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.viralsim.metrics.CalculadorMetricas;
import com.viralsim.models.Arista;
import com.viralsim.models.EstadoCatalogo;
import com.viralsim.models.Grafo;
import com.viralsim.models.Nodo;
import com.viralsim.repositories.AristaRepository;
import com.viralsim.repositories.EstadoCatalogoRepository;
import com.viralsim.repositories.GrafoRepository;
import com.viralsim.repositories.NodoRepository;

@Component
public class WattsStrogatzGenerator {
    
    @Autowired private GrafoRepository grafoRepository;
    @Autowired private NodoRepository nodoRepository;
    @Autowired private AristaRepository aristaRepository;
    @Autowired private EstadoCatalogoRepository estadoRepository;
    @Autowired private CalculadorMetricas calculadorMetricas;

    private final Random random = new Random();

    public Grafo crearGrafo() {
        Grafo grafo = new Grafo();
        grafo.setTotalNodos(250);
        grafo = grafoRepository.save(grafo);
        final int grafoId = grafo.getId();

        EstadoCatalogo estadoNoInformado = estadoRepository.findById(0).orElseThrow();
        EstadoCatalogo estadoInformado = estadoRepository.findById(2).orElseThrow();
        EstadoCatalogo estadoResistente = estadoRepository.findById(3).orElseThrow();

        List<Nodo> nodos = new ArrayList<>();

        for (int i = 0; i < 250; i++) {
            Nodo nodo = new Nodo();
            nodo.setGrafo(grafo);
            nodo.setNombre("Nodo_" +(i+1));
            nodo.setCentralidadGrado(0.0);
            nodo.setBetweenness(0.0);

            double r = random.nextDouble();
            if (r < 0.10) {
                nodo.setEstado(estadoResistente);
                nodo.setProbabilidadPropagacion(random.nextDouble() * 0.3);
            } else if (r < 0.40) {
                nodo.setEstado(estadoInformado);
                nodo.setProbabilidadPropagacion(random.nextDouble() * 0.3);
            } else {
                nodo.setEstado(estadoNoInformado);
                nodo.setProbabilidadPropagacion(0.6 + random.nextDouble() * 0.3);
            }

            double u = random.nextDouble();
            if (u < 0.30) {
                nodo.setUmbral(0.1 + random.nextDouble() * 0.2);
            } else if (u < 0.80) {
                nodo.setUmbral(0.3 + random.nextDouble() * 0.4);
            } else {
                nodo.setUmbral(0.6 + random.nextDouble() * 0.3);
            }

            nodos.add(nodoRepository.save(nodo));
        }

        Set<String> aristasCreadas = new HashSet<>();

        int k = 6;

        for (int i = 0; i < 250; i++) {
            for (int delta = 1; delta <= k/2; delta++) {
                int j = (i + delta) % 250;

                int menor = Math.min(i, j);
                int mayor = Math.max(i, j);
                String clave = menor + "_" + mayor;

                if (!aristasCreadas.contains(clave)) {
                    Arista arista = new Arista();
                    arista.setNodoOrigen(nodos.get(i));
                    arista.setNodoDestino(nodos.get(j));
                    arista.setPeso(1.0);
                    arista.setProbabilidadArista(0.5);
                    arista.setActiva(true);
                    aristaRepository.save(arista);
                    aristasCreadas.add(clave);
                }
            }
        }

        double p = 0.1;

        for (int i = 0; i < 250; i++) {
            for (int delta = 1; delta <= k/2; delta++) {
                if (random.nextDouble() < p) {

                    int intentos = 0;
                    while (intentos < 50) {
                        int nuevoJ = random.nextInt(250);
                        int menor = Math.min(i, nuevoJ);
                        int mayor = Math.max(i, nuevoJ);
                        String clave = menor + "_" + mayor;

                        if (nuevoJ != i && !aristasCreadas.contains(clave)) {
                            Arista arista = new Arista();
                            arista.setNodoOrigen(nodos.get(i));
                            arista.setNodoDestino(nodos.get(nuevoJ));
                            arista.setPeso(1.0);
                            arista.setProbabilidadArista(0.5);
                            arista.setActiva(true);
                            aristaRepository.save(arista);
                            aristasCreadas.add(clave);
                            break;
                        }
                        intentos++;
                    }
                }
            }
        }

        // Calcular métricas de centralidad después de generar la red completa
        List<Nodo> nodosTotales = nodoRepository.findAll().stream()
                .filter(n -> n.getGrafo().getId().equals(grafoId))
                .toList();
        List<Arista> aristasTotales = aristaRepository.findAll().stream()
                .filter(a -> a.getNodoOrigen().getGrafo().getId().equals(grafoId))
                .toList();
        
        calculadorMetricas.calcularTodasLasMetricas(grafo, nodosTotales, aristasTotales);

        return grafo;
    }
}
