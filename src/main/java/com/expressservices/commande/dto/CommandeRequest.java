package com.expressservices.commande.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommandeRequest {
    private String nomClient;
    private String telephoneClient;
    private String emailClient;
    private List<LigneProduitRequest> lignesProduits;
    private Long quartierId;
    private String adressePrecise;
    private Double latitude;
    private Double longitude;
    private LocalDateTime dateHeureSouhaitee;
    private Long partenaireId;
    private String descriptionArticle;
    private Boolean livraisonGratuite;
}
