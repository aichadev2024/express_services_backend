package com.expressservices.produit.repository;

import com.expressservices.produit.model.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProduitRepository extends JpaRepository<Produit, Long> {
    List<Produit> findByActifTrue();
    boolean existsByNom(String nom);
    List<Produit> findByPartenaireId(Long partenaireId);
    List<Produit> findByActifTrueAndPartenaireId(Long partenaireId);
}
