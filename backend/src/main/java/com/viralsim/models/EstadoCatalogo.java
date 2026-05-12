package com.viralsim.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Estado")
@Data
@NoArgsConstructor
public class EstadoCatalogo {
    
    @Id
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String nombre;
}
