package com.expressservices.commande.repository;

import com.expressservices.commande.model.CommandeProduit;
import com.expressservices.commande.model.StatutCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommandeProduitRepository extends JpaRepository<CommandeProduit, Long> {
    List<CommandeProduit> findByCommandeId(Long commandeId);

    @Query("SELECT COALESCE(SUM(cp.quantite), 0) FROM CommandeProduit cp WHERE cp.produit.id = :produitId AND cp.commande.statut IN :statuts")
    Integer sumQuantiteByProduitIdAndStatutIn(Long produitId, List<StatutCommande> statuts);
}
