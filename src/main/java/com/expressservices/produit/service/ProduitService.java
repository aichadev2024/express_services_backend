package com.expressservices.produit.service;

import com.expressservices.commande.model.StatutCommande;
import com.expressservices.commande.repository.CommandeProduitRepository;
import com.expressservices.exception.ResourceNotFoundException;
import com.expressservices.partenaire.service.PartenaireService;
import com.expressservices.partenaire.model.Partenaire;
import com.expressservices.produit.dto.ProduitRequest;
import com.expressservices.produit.dto.ProduitResponse;
import com.expressservices.produit.dto.ProduitStockStats;
import com.expressservices.produit.model.Produit;
import com.expressservices.produit.repository.ProduitRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProduitService {

    private final ProduitRepository produitRepository;
    /**
     * Exception pragmatique a la regle "un module ne touche pas au repository d'un autre module" :
     * lecture seule pour agreger les quantites par statut de commande (stats de stock). La faire
     * passer par CommandeService creerait une dependance circulaire (commande -> produit pour le
     * decompte de stock, produit -> commande pour ces stats).
     */
    private final CommandeProduitRepository commandeProduitRepository;
    private final PartenaireService partenaireService;

    public ProduitService(ProduitRepository produitRepository, CommandeProduitRepository commandeProduitRepository,
                          PartenaireService partenaireService) {
        this.produitRepository = produitRepository;
        this.commandeProduitRepository = commandeProduitRepository;
        this.partenaireService = partenaireService;
    }

    @PostConstruct
    public void cleanUpDefaultProduits() {
        try {
            List<Produit> testProds = produitRepository.findAll().stream()
                    .filter(p -> p.getPartenaire() == null)
                    .collect(Collectors.toList());
            if (!testProds.isEmpty()) {
                List<com.expressservices.commande.model.CommandeProduit> relatedLines =
                        commandeProduitRepository.findAll().stream()
                                .filter(cp -> cp.getProduit() != null && testProds.contains(cp.getProduit()))
                                .collect(Collectors.toList());
                if (!relatedLines.isEmpty()) {
                    commandeProduitRepository.deleteAll(relatedLines);
                }
                produitRepository.deleteAll(testProds);
                System.out.println("====== SYSTEM CLEANUP: Removed default test products with no partner ======");
            }
        } catch (Exception e) {
            System.err.println("Could not clean up default products: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<ProduitResponse> getAllProduits(boolean actifSeulement, Long partenaireId) {
        List<Produit> produits;
        if (partenaireId != null) {
            produits = actifSeulement
                    ? produitRepository.findByActifTrueAndPartenaireId(partenaireId)
                    : produitRepository.findByPartenaireId(partenaireId);
        } else {
            produits = actifSeulement ? produitRepository.findByActifTrue() : produitRepository.findAll();
        }
        return produits.stream().map(ProduitResponse::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProduitResponse getProduitById(Long id) {
        return ProduitResponse.fromEntity(getProduitEntityById(id));
    }

    /**
     * Reservee aux autres modules (ex: commande) pour resoudre un produit sans
     * dupliquer l'acces au repository.
     */
    @Transactional(readOnly = true)
    public Produit getProduitEntityById(Long id) {
        return produitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable avec ID: " + id));
    }

    /**
     * Reservee au module commande : decremente le stock d'un produit, ou refuse (400)
     * si le stock disponible est insuffisant.
     */
    public void decrementStock(Long produitId, int quantite) {
        Produit produit = getProduitEntityById(produitId);
        if (produit.getStock() < quantite) {
            throw new IllegalArgumentException(
                    "Stock insuffisant pour le produit : " + produit.getNom() + " (Disponible: " + produit.getStock() + ")");
        }
        produit.setStock(produit.getStock() - quantite);
        produitRepository.save(produit);
    }

    /**
     * Reservee au module commande : restaure le stock d'un produit (annulation, retour, etc.).
     */
    public void restoreStock(Long produitId, int quantite) {
        Produit produit = getProduitEntityById(produitId);
        produit.setStock(produit.getStock() + quantite);
        produitRepository.save(produit);
    }

    @Transactional(readOnly = true)
    public List<ProduitStockStats> getProduitStockStats() {
        return buildStockStats(produitRepository.findAll());
    }

    /**
     * Vue scopee pour un Partenaire resident (sans compte) : ne renvoie jamais les
     * stats d'un autre partenaire, uniquement celles de ses propres produits.
     */
    @Transactional(readOnly = true)
    public List<ProduitStockStats> getProduitStockStatsForPartenaire(Long partenaireId) {
        return buildStockStats(produitRepository.findByPartenaireId(partenaireId));
    }

    private List<ProduitStockStats> buildStockStats(List<Produit> produits) {
        return produits.stream().map(p -> {
            Integer sortis = commandeProduitRepository.sumQuantiteByProduitIdAndStatutIn(
                    p.getId(), 
                    Arrays.asList(StatutCommande.EN_ATTENTE, StatutCommande.EN_COURS, StatutCommande.LIVREE)
            );
            
            Integer retournes = commandeProduitRepository.sumQuantiteByProduitIdAndStatutIn(
                    p.getId(), 
                    Arrays.asList(
                            StatutCommande.ANNULEE, 
                            StatutCommande.REJETEE, 
                            StatutCommande.REPORTEE, 
                            StatutCommande.INJOIGNABLE
                    )
            );

            return ProduitStockStats.builder()
                    .id(p.getId())
                    .nom(p.getNom())
                    .stockDisponible(p.getStock())
                    .sortisPourLivraison(sortis)
                    .restants(p.getStock())
                    .retournes(retournes)
                    .build();
        }).collect(Collectors.toList());
    }

    public ProduitResponse createProduit(ProduitRequest request) {
        Produit produit = Produit.builder()
                .nom(request.getNom())
                .prix(request.getPrix())
                .description(request.getDescription())
                .stock(request.getStock() != null ? request.getStock() : 0)
                .actif(request.isActif())
                .partenaire(resolvePartenaire(request.getPartenaireId()))
                .build();
        return ProduitResponse.fromEntity(produitRepository.save(produit));
    }

    public ProduitResponse updateProduit(Long id, ProduitRequest request) {
        Produit produit = getProduitEntityById(id);
        produit.setNom(request.getNom());
        produit.setPrix(request.getPrix());
        produit.setDescription(request.getDescription());
        produit.setStock(request.getStock());
        produit.setActif(request.isActif());
        produit.setPartenaire(resolvePartenaire(request.getPartenaireId()));
        return ProduitResponse.fromEntity(produitRepository.save(produit));
    }

    public ProduitResponse toggleActif(Long id) {
        Produit produit = getProduitEntityById(id);
        produit.setActif(!produit.isActif());
        return ProduitResponse.fromEntity(produitRepository.save(produit));
    }

    public void deleteProduit(Long id) {
        Produit produit = getProduitEntityById(id);
        produitRepository.delete(produit);
    }

    private Partenaire resolvePartenaire(Long partenaireId) {
        return partenaireId != null ? partenaireService.getPartenaireEntityById(partenaireId) : null;
    }
}
