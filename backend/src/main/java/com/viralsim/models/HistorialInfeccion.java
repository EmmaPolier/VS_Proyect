package com.viralsim.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "historial_infeccion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistorialInfeccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "simulacion_id", nullable = false)
    private Long simulacionId;

    @Column(nullable = false)
    private Integer paso;

    @Column(name = "nodo_id", nullable = false)
    private Long nodoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", nullable = false)
    private Estado estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", nullable = false)
    private Estado estadoNuevo;

    @Column(name = "padre_id")
    private Long padreId;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;
}