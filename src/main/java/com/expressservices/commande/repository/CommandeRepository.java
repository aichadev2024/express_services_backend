package com.expressservices.commande.repository;

import com.expressservices.commande.model.Commande;
import com.expressservices.commande.model.StatutCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommandeRepository extends JpaRepository<Commande, Long> {
    List<Commande> findByLivreurIdOrderByDateCreationDesc(Long livreurId);
    List<Commande> findByStatutOrderByDateCreationDesc(StatutCommande statut);
    List<Commande> findByLivreurIdAndStatutOrderByDateCreationDesc(Long livreurId, StatutCommande statut);
    List<Commande> findAllByOrderByDateCreationDesc();
    List<Commande> findByDateCreationAfter(java.time.LocalDateTime date);
    long countByStatut(StatutCommande statut);
    List<Commande> findByTelephoneClientContaining(String telephone);
    List<Commande> findByNomClientContainingIgnoreCase(String nom);
    List<Commande> findByPartenaireNomContainingIgnoreCase(String partenaireNom);
}
