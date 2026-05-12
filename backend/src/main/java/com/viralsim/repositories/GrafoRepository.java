package com.viralsim.repositories;

import com.viralsim.models.Grafo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GrafoRepository extends JpaRepository<Grafo, Integer> {
}