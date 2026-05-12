package com.viralsim.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Arista",
        uniqueConstraints = @UniqueConstraint(columnNames = {"nodoOrigenId", "nodoDestinoId"}))
@Data
@NoArgsConstructor
public class Arista {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "nodoOrigenId", nullable = false)
    private Nodo nodoOrigen;

    @ManyToOne(optional = false)
    @JoinColumn(name = "nodoDestinoId", nullable = false)
    private Nodo nodoDestino;

    @Column(nullable = false)
    private Double probabilidadArista = 0.5;

    @Column(nullable = false)
    private Double peso = 1.0;

    @Column(nullable = false)
    private boolean activa = true;
}
