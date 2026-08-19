package com.expressservices.produit.model;

import com.expressservices.partenaire.model.Partenaire;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "produits")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal prix;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Integer stock = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean actif = true;

    /**
     * Boutique proprietaire de ce produit (stock propre a chaque partenaire).
     * Nullable : un produit peut ne pas encore etre rattache a un partenaire.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "partenaire_id")
    private Partenaire partenaire;
}
