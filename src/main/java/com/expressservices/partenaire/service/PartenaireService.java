package com.expressservices.partenaire.service;

import com.expressservices.exception.ResourceNotFoundException;
import com.expressservices.partenaire.dto.PartenaireRequest;
import com.expressservices.partenaire.dto.PartenaireResponse;
import com.expressservices.partenaire.model.Partenaire;
import com.expressservices.partenaire.repository.PartenaireRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PartenaireService {

    private final PartenaireRepository partenaireRepository;

    public PartenaireService(PartenaireRepository partenaireRepository) {
        this.partenaireRepository = partenaireRepository;
    }

    public List<PartenaireResponse> getAllPartenaires() {
        return partenaireRepository.findAll().stream()
                .map(PartenaireResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public PartenaireResponse getPartenaireById(Long id) {
        return PartenaireResponse.fromEntity(getPartenaireEntityById(id));
    }

    public PartenaireResponse createPartenaire(PartenaireRequest request) {
        if (partenaireRepository.existsByNom(request.getNom())) {
            throw new IllegalArgumentException("Un partenaire avec ce nom existe déjà");
        }
        Partenaire partenaire = Partenaire.builder()
                .nom(request.getNom())
                .telephone(request.getTelephone())
                .build();
        return PartenaireResponse.fromEntity(partenaireRepository.save(partenaire));
    }

    public PartenaireResponse updatePartenaire(Long id, PartenaireRequest request) {
        Partenaire partenaire = getPartenaireEntityById(id);

        if (!partenaire.getNom().equalsIgnoreCase(request.getNom())
                && partenaireRepository.existsByNom(request.getNom())) {
            throw new IllegalArgumentException("Un partenaire avec ce nom existe déjà");
        }

        partenaire.setNom(request.getNom());
        partenaire.setTelephone(request.getTelephone());
        return PartenaireResponse.fromEntity(partenaireRepository.save(partenaire));
    }

    /**
     * Reservee aux autres modules (ex: produit, commande) pour valider une reference
     * a un partenaire sans dupliquer l'acces au repository.
     */
    public Partenaire getPartenaireEntityById(Long id) {
        return partenaireRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partenaire introuvable avec ID: " + id));
    }
}
