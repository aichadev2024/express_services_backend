package com.expressservices.produit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProduitRequest {
    private String nom;
    private BigDecimal prix;
    private String description;
    private Integer stock;
    private boolean actif;
    private Long partenaireId;
}
