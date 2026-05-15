package com.viralsim.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "PasoSimulacion",
       uniqueConstraints = @UniqueConstraint(columnNames = {"simulacionId", "numeroPaso"}))
@Data
@NoArgsConstructor
public class PasoSimulacion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "simulacionId", nullable = false)
    private Simulacion simulacion;
    
    @Column(nullable = false)
    private Integer numeroPaso;

    @Column(nullable = false)
    private Integer nuevosInformados = 0;
    
    @Column(nullable = false)
    private Integer totalActivos = 0;
    
    @Column(nullable = false)
    private Integer totalResistentes = 0;

    @Column(nullable = false)
    private Integer totalPasivos = 0;

    @Column(nullable = false)
    private Integer totalInformados = 0;
}
