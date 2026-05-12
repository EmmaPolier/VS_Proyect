package com.viralsim.repositories;

import com.viralsim.models.Simulacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SimulacionRepository extends JpaRepository<Simulacion, Integer> {
}
