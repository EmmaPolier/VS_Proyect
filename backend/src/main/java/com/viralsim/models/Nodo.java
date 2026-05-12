package com.viralsim.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Nodo",
        uniqueConstraints = @UniqueConstraint(columnNames = {"grafoId", "nombre"}))
@Data
@NoArgsConstructor
public class Nodo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "grafoId", nullable = false)
    private Grafo grafo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "estadoId", nullable = false)
    private EstadoCatalogo estado;

    @ManyToOne
    @JoinColumn(name = "padreId")
    private Nodo padre;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false)
    private Double probabilidadPropagacion = 0.0;

    @Column(nullable = false)
    private Double umbral = 0.5;

    @Column(nullable = false)
    private Double centralidadGrado = 0.0;

    @Column(nullable = false)
    private Double betweenness = 0.0;
}
