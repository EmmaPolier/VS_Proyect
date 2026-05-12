package com.viralsim.repositories;

import com.viralsim.models.PasoSimulacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasoSimulacionRepository extends JpaRepository<PasoSimulacion, Integer> {
}
