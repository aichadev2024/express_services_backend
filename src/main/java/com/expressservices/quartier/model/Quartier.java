package com.expressservices.quartier.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "quartiers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quartier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String nom;

    @Column(name = "tarif_livraison", nullable = false)
    private Double tarifLivraison;
}
