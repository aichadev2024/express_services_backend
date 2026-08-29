package com.expressservices.partenaire.controller;

import com.expressservices.partenaire.dto.PartenaireRequest;
import com.expressservices.partenaire.dto.PartenaireResponse;
import com.expressservices.partenaire.service.PartenaireService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/partenaires")
public class PartenaireController {

    private final PartenaireService partenaireService;

    public PartenaireController(PartenaireService partenaireService) {
        this.partenaireService = partenaireService;
    }

    @GetMapping
    public ResponseEntity<List<PartenaireResponse>> getAllPartenaires() {
        return ResponseEntity.ok(partenaireService.getAllPartenaires());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PartenaireResponse> getPartenaireById(@PathVariable Long id) {
        return ResponseEntity.ok(partenaireService.getPartenaireById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<PartenaireResponse> createPartenaire(@RequestBody PartenaireRequest request) {
        return ResponseEntity.ok(partenaireService.createPartenaire(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<PartenaireResponse> updatePartenaire(@PathVariable Long id, @RequestBody PartenaireRequest request) {
        return ResponseEntity.ok(partenaireService.updatePartenaire(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deletePartenaire(@PathVariable Long id) {
        partenaireService.deletePartenaire(id);
        return ResponseEntity.noContent().build();
    }
}
