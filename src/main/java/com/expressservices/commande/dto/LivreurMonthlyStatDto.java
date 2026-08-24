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
public class LivreurMonthlyStatDto {
    private Long livreurId;
    private String livreurUsername;
    private String livreurNom;
    private String livreurPrenom;
    private String livreurTelephone;
    private long nombreLivraisons;
    private BigDecimal totalFraisEncaisse;
    private BigDecimal totalMarchandisesLivrees;
    private int rang;
}
