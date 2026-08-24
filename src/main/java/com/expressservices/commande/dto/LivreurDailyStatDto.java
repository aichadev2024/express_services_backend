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
public class LivreurDailyStatDto {
    private Long livreurId;
    private String livreurUsername;
    private String livreurNom;
    private String livreurPrenom;
    private String livreurTelephone;
    private long nombreLivraisonsAssignees;
    private long nombreLivraisonsLivrees;
    private long totalLivraisonsGratuites;
    private BigDecimal totalFraisLivraison;
    private BigDecimal totalMontantMarchandises;
    private BigDecimal totalMontantGlobal;
}
