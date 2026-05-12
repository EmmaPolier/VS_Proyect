package com.viralsim.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ConfiguracionSimulacion")
@Data
@NoArgsConstructor
public class ConfiguracionSimulacion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(optional = false)
    @JoinColumn(name = "simulacionId", nullable = false, unique = true)
    private Simulacion simulacion;

    @Column(nullable = false)
    private Double probabilidadGlobal = 0.5;

    @Column(nullable = false)
    private Integer velocidadAnimacion = 500;
}
