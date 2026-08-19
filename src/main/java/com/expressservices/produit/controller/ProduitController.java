package com.expressservices.produit.controller;

import com.expressservices.produit.dto.ProduitRequest;
import com.expressservices.produit.dto.ProduitResponse;
import com.expressservices.produit.dto.ProduitStockStats;
import com.expressservices.produit.service.ProduitService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produits")
public class ProduitController {

    private final ProduitService produitService;

    public ProduitController(ProduitService produitService) {
        this.produitService = produitService;
    }

    /**
     * Liste les produits actifs (publique, pour le formulaire de commande).
     * L'admin peut voir tous les produits via ?actifSeulement=false
     * Le Partenaire resident filtre sur son propre stock via ?partenaireId=X
     */
    @GetMapping
    public ResponseEntity<List<ProduitResponse>> getAllProduits(
            @RequestParam(defaultValue = "true") boolean actifSeulement,
            @RequestParam(required = false) Long partenaireId) {
        return ResponseEntity.ok(produitService.getAllProduits(actifSeulement, partenaireId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProduitResponse> getProduitById(@PathVariable Long id) {
        return ResponseEntity.ok(produitService.getProduitById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ProduitResponse> createProduit(@RequestBody ProduitRequest request) {
        return ResponseEntity.ok(produitService.createProduit(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ProduitResponse> updateProduit(@PathVariable Long id, @RequestBody ProduitRequest request) {
        return ResponseEntity.ok(produitService.updateProduit(id, request));
    }

    @PatchMapping("/{id}/toggle-actif")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ProduitResponse> toggleActif(@PathVariable Long id) {
        return ResponseEntity.ok(produitService.toggleActif(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteProduit(@PathVariable Long id) {
        produitService.deleteProduit(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<ProduitStockStats>> getProduitStockStats() {
        return ResponseEntity.ok(produitService.getProduitStockStats());
    }

    /**
     * Vue stock scopee pour un Partenaire resident (pas de compte, donc pas de JWT) :
     * toujours filtree par l'ID fourni, jamais de vue globale sans partenaireId.
     */
    @GetMapping("/stats/partenaire/{partenaireId}")
    public ResponseEntity<List<ProduitStockStats>> getProduitStockStatsForPartenaire(@PathVariable Long partenaireId) {
        return ResponseEntity.ok(produitService.getProduitStockStatsForPartenaire(partenaireId));
    }
}
