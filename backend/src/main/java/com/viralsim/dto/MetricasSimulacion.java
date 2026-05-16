package com.viralsim.dto;

import java.time.LocalDateTime;
import java.util.List;
import com.viralsim.models.Nodo;


/**
 * DTO para devolver métricas de una simulación ejecutada.
 */
public record MetricasSimulacion(
    Integer simulacionId,
    Integer grafoId,
    Integer modeloId,
    Integer nodoSemillaId,
    LocalDateTime iniciadaEn,
    Integer totalPasos,
    Integer totalInformados,
    Integer paso50Porciento,
    String resultado,
    Double alcancePorcentaje,
    List<Nodo> topGrado,
    List<Nodo> topBetweenness
) {
}
