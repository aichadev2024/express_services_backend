package com.expressservices.produit.dto;

import com.expressservices.produit.model.Produit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProduitResponse {
    private Long id;
    private String nom;
    private BigDecimal prix;
    private String description;
    private Integer stock;
    private boolean actif;
    private Long partenaireId;
    private String partenaireNom;

    public static ProduitResponse fromEntity(Produit produit) {
        return ProduitResponse.builder()
                .id(produit.getId())
                .nom(produit.getNom())
                .prix(produit.getPrix())
                .description(produit.getDescription())
                .stock(produit.getStock())
                .actif(produit.isActif())
                .partenaireId(produit.getPartenaire() != null ? produit.getPartenaire().getId() : null)
                .partenaireNom(produit.getPartenaire() != null ? produit.getPartenaire().getNom() : null)
                .build();
    }
}
