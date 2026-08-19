package com.expressservices.quartier.controller;

import com.expressservices.quartier.model.Quartier;
import com.expressservices.quartier.service.QuartierService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quartiers")
public class QuartierController {

    private final QuartierService quartierService;

    public QuartierController(QuartierService quartierService) {
        this.quartierService = quartierService;
    }

    @GetMapping
    public ResponseEntity<List<Quartier>> getAllQuartiers() {
        return ResponseEntity.ok(quartierService.getAllQuartiers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Quartier> getQuartierById(@PathVariable Long id) {
        return ResponseEntity.ok(quartierService.getQuartierById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Quartier> createQuartier(@RequestBody Quartier quartier) {
        return ResponseEntity.ok(quartierService.createQuartier(quartier));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Quartier> updateQuartier(@PathVariable Long id, @RequestBody Quartier quartierDetails) {
        return ResponseEntity.ok(quartierService.updateQuartier(id, quartierDetails));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteQuartier(@PathVariable Long id) {
        quartierService.deleteQuartier(id);
        return ResponseEntity.noContent().build();
    }
}
