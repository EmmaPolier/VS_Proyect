package com.viralsim.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "Simulacion")
@Data
@NoArgsConstructor
public class Simulacion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "grafoId", nullable = false)
    private Grafo grafo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "modeloId", nullable = false)
    private ModeloPropagacion modelo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "nodoSemillaId", nullable = false)
    private Nodo nodoSemilla;

    @Column(nullable = false, updatable = false)
    private LocalDateTime iniciadaEn;

    @Column(nullable = false)
    private Integer totalPasos = 0;

    @Column(nullable = false)
    private Integer totalInformados = 0;

    private Integer paso50Porciento;

    @Column(columnDefinition = "TEXT")
    private String resultado;

    @PrePersist
    protected void onCreate() {
        iniciadaEn = LocalDateTime.now();
    }
}
