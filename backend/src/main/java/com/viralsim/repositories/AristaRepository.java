package com.viralsim.repositories;

import com.viralsim.models.Arista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AristaRepository extends JpaRepository<Arista, Integer> {
}