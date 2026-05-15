package com.viralsim.repositories;

import com.viralsim.models.Nodo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NodoRepository extends JpaRepository<Nodo, Integer> {
    
    List<Nodo> findByGrafo_Id(Integer grafoId);
}
