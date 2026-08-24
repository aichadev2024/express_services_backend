package com.expressservices.commande.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyHistoryStatDto {
    private LocalDate date;
    private long totalLivraisons;
    private long totalCommandesLivrees;
    private long totalLivraisonsGratuites;
    private BigDecimal totalFraisLivraison;
    private BigDecimal totalMontantMarchandises;
    private BigDecimal totalMontantGlobal;
}
