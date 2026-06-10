package com.viralsim.dto;

public record NodoUpdateDTO(
    String nombre,
    Double probabilidad,
    Double probabilidadPropagacion,
    Double umbral,
    String estado,
    Integer estadoId,
    Double resistencia
) {
}
