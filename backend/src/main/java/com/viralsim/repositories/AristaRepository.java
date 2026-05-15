package com.viralsim.repositories;

import com.viralsim.models.Arista;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AristaRepository extends JpaRepository<Arista, Integer> {
    
    @Query("SELECT a FROM Arista a WHERE a.nodoOrigen.grafo.id = :grafoId OR a.nodoDestino.grafo.id = :grafoId")
    List<Arista> findByGrafo_Id(@Param("grafoId") Integer grafoId);
}