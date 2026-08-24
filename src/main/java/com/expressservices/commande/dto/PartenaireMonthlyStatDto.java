package com.expressservices.commande.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartenaireMonthlyStatDto {
    private Long partenaireId;
    private String partenaireNom;
    private long nombreCommandes;
    private BigDecimal gainsPartenaire;
    private double pourcentageDuTotal;
}
