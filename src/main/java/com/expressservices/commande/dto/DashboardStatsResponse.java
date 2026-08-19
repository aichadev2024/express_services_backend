package com.expressservices.commande.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsResponse {
    private long totalCommandesDuJour;
    private long commandesEnAttente;
    private long commandesEnCours;
    private long commandesLivrees;
    private long commandesAnnulees;
    private long commandesRejetees;
    private long commandesReportees;
    private long commandesInjoignables;
    private BigDecimal montantTotalDuJour;
}
