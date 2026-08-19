package com.expressservices.produit.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProduitStockStats {
    private Long id;
    private String nom;
    private Integer stockDisponible;     // Available stock in depot (same as restants)
    private Integer sortisPourLivraison; // Sum of quantities in EN_COURS orders
    private Integer restants;            // Remaining stock in depot
    private Integer retournes;           // Sum of quantities in ANNULEE, REJETEE, REPORTEE, INJOIGNABLE orders
}
