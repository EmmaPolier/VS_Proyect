package com.viralsim.repositories;

import com.viralsim.models.ModeloPropagacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModeloPropagacionRepository extends JpaRepository<ModeloPropagacion, Integer> {
}
