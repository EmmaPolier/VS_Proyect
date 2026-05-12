package com.viralsim.repositories;

import com.viralsim.models.Nodo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NodoRepository extends JpaRepository<Nodo, Integer> {
}
