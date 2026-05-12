package com.viralsim.controllers;

import com.viralsim.models.Arista;
import com.viralsim.services.AristaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/aristas")
public class AristaController {

    private final AristaService aristaService;

    public AristaController(AristaService aristaService) {
        this.aristaService = aristaService;
    }

    @PostMapping
    public ResponseEntity<Arista> crear(@RequestParam int nodoOrigenId,
            @RequestParam int nodoDestinoId) {
        return ResponseEntity.ok(aristaService.crearArista(nodoOrigenId, nodoDestinoId));
    }

    @GetMapping
    public ResponseEntity<List<Arista>> obtenerTodas() {
        return ResponseEntity.ok(aristaService.obtenerTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Arista> obtenerPorId(@PathVariable int id) {
        return ResponseEntity.ok(aristaService.obtenerPorId(id));
    }
}
