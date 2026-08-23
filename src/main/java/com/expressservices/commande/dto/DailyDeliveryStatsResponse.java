package com.expressservices.commande.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyDeliveryStatsResponse {
    private LocalDate date;
    private long totalLivraisonsDuJour;
    private long nombreLivraisonsLivrees;
    private BigDecimal totalFraisLivraison;
    private BigDecimal totalMontantMarchandises;
    private BigDecimal totalMontantGlobal;
    private List<LivreurDailyStatDto> livreursStats;
    private List<DailyHistoryStatDto> historique7Jours;
}
