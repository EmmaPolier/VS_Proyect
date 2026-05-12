package com.viralsim.repositories;

import com.viralsim.models.ConfiguracionSimulacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfiguracionSimulacionRepository extends JpaRepository<ConfiguracionSimulacion, Integer> {
}
