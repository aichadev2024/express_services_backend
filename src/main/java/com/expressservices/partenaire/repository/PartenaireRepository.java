package com.expressservices.partenaire.repository;

import com.expressservices.partenaire.model.Partenaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PartenaireRepository extends JpaRepository<Partenaire, Long> {
    boolean existsByNom(String nom);
    java.util.Optional<Partenaire> findByNom(String nom);
}
