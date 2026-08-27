package com.expressservices.commande.model;

import com.expressservices.auth.model.User;
import com.expressservices.partenaire.model.Partenaire;
import com.expressservices.quartier.model.Quartier;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "commandes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Commande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom_client", nullable = false)
    private String nomClient;

    @Column(name = "telephone_client", nullable = false)
    private String telephoneClient;

    @Column(name = "email_client")
    private String emailClient;

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<CommandeProduit> lignesProduits = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "quartier_id", nullable = false)
    private Quartier quartier;

    @Column(name = "adresse_precise", nullable = false)
    private String adressePrecise;

    private Double latitude;

    private Double longitude;

    @Column(name = "date_heure_souhaitee")
    private LocalDateTime dateHeureSouhaitee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutCommande statut;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "livreur_id")
    private User livreur;

    /**
     * Partenaire proprietaire de la marchandise (optionnel).
     * Absent => commande "Particulier" : descriptionArticle est utilisee a la place des lignes produits.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "partenaire_id")
    private Partenaire partenaire;

    @Column(name = "description_article", columnDefinition = "TEXT")
    private String descriptionArticle;

    @Column(name = "motif_annulation", columnDefinition = "TEXT")
    private String motifAnnulation;

    @Column(name = "livraison_gratuite")
    @Builder.Default
    private Boolean livraisonGratuite = false;

    @Column(name = "montant_marchandises", precision = 10, scale = 2)
    private BigDecimal montantMarchandises;

    @PrePersist
    protected void onCreate() {
        this.dateCreation = LocalDateTime.now();
        if (this.statut == null) {
            this.statut = StatutCommande.EN_ATTENTE;
        }
        if (this.livraisonGratuite == null) {
            this.livraisonGratuite = false;
        }
    }

    @Transient
    public BigDecimal getMontantProduits() {
        if (lignesProduits != null && !lignesProduits.isEmpty()) {
            return lignesProduits.stream()
                    .map(CommandeProduit::getSousTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        return montantMarchandises != null ? montantMarchandises : BigDecimal.ZERO;
    }

    @Transient
    public BigDecimal getTarifLivraisonEffective() {
        if (Boolean.TRUE.equals(livraisonGratuite)) {
            return BigDecimal.ZERO;
        }
        return quartier != null ? BigDecimal.valueOf(quartier.getTarifLivraison()) : BigDecimal.ZERO;
    }

    @Transient
    public BigDecimal getMontantTotal() {
        return getMontantProduits().add(getTarifLivraisonEffective());
    }
}
