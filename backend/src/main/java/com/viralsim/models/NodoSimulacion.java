package com.viralsim.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "NodoSimulacion",
        uniqueConstraints = @UniqueConstraint(columnNames = {"pasoId", "nodoId"}))
@Data
@NoArgsConstructor
public class NodoSimulacion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "estadoId", nullable = false)
    private EstadoCatalogo estado;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pasoId", nullable = false)
    private PasoSimulacion paso;

    @ManyToOne(optional = false)
    @JoinColumn(name = "nodoId", nullable = false)
    private Nodo nodo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "simulacionId", nullable = false)
    private Simulacion simulacion;

    private Integer pasoInfeccion;
}
