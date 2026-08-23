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
    List<Commande> findByDateCreationBetweenOrderByDateCreationDesc(java.time.LocalDateTime start, java.time.LocalDateTime end);
    List<Commande> findByStatutAndDateCreationBetweenOrderByDateCreationDesc(StatutCommande statut, java.time.LocalDateTime start, java.time.LocalDateTime end);
    List<Commande> findByLivreurIdAndDateCreationBetweenOrderByDateCreationDesc(Long livreurId, java.time.LocalDateTime start, java.time.LocalDateTime end);
    List<Commande> findByLivreurIdAndStatutAndDateCreationBetweenOrderByDateCreationDesc(Long livreurId, StatutCommande statut, java.time.LocalDateTime start, java.time.LocalDateTime end);
    List<Commande> findByDateCreationAfter(java.time.LocalDateTime date);
    long countByStatut(StatutCommande statut);
    List<Commande> findByTelephoneClientContaining(String telephone);
    List<Commande> findByNomClientContainingIgnoreCase(String nom);
    List<Commande> findByPartenaireNomContainingIgnoreCase(String partenaireNom);
}
