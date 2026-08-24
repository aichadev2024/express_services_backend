package com.expressservices.commande.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommandeResponse {
    private Long id;
    private String nomClient;
    private String telephoneClient;
    private String emailClient;
    private List<LigneProduitResponse> lignesProduits;
    private Long quartierId;
    private String quartierNom;
    private Double tarifLivraison;
    private String adressePrecise;
    private Double latitude;
    private Double longitude;
    private LocalDateTime dateHeureSouhaitee;
    private String statut;
    private LocalDateTime dateCreation;
    private Long livreurId;
    private String livreurUsername;
    private String livreurNom;
    private String livreurPrenom;
    private BigDecimal montantProduits;
    private BigDecimal montantTotal;
    private Long partenaireId;
    private String partenaireNom;
    private String descriptionArticle;
    private String motifAnnulation;
    private Boolean livraisonGratuite;
    private Double tarifLivraisonEffective;
    private BigDecimal montantAEncaisser;
}
