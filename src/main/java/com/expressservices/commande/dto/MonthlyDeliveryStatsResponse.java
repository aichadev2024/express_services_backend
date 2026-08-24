package com.expressservices.commande.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyDeliveryStatsResponse {
    private int year;
    private int month;
    private long totalLivraisons;
    private long totalCommandesLivrees;
    private long totalLivraisonsGratuites;
    private BigDecimal gainsPlateforme; // Delivery fees collected (admin)
    private BigDecimal gainsMarchandises; // Total merchandise sold
    private BigDecimal gainsGlobal; // Sum of platform + merchandise
    private List<PartenaireMonthlyStatDto> partenairesStats;
    private List<LivreurMonthlyStatDto> topLivreurs;
}
