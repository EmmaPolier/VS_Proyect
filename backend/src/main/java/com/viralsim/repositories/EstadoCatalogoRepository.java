package com.viralsim.repositories;

import com.viralsim.models.EstadoCatalogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstadoCatalogoRepository extends JpaRepository<EstadoCatalogo, Integer> {
}

