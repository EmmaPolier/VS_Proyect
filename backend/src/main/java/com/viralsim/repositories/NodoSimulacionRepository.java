package com.viralsim.repositories;

import com.viralsim.models.NodoSimulacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NodoSimulacionRepository extends JpaRepository<NodoSimulacion, Integer> {
}
