package com.expressservices.quartier.service;

import com.expressservices.exception.ResourceNotFoundException;
import com.expressservices.quartier.model.Quartier;
import com.expressservices.quartier.repository.QuartierRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuartierService {

    private final QuartierRepository quartierRepository;

    public QuartierService(QuartierRepository quartierRepository) {
        this. quartierRepository = quartierRepository;
    }



    public List<Quartier> getAllQuartiers() {
        return quartierRepository.findAll();
    }

    public Quartier getQuartierById(Long id) {
        return quartierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quartier introuvable avec ID: " + id));
    }

    public Quartier createQuartier(Quartier quartier) {
        if (quartierRepository.existsByNom(quartier.getNom())) {
            throw new IllegalArgumentException("Quartier name already exists");
        }
        return quartierRepository.save(quartier);
    }

    public Quartier updateQuartier(Long id, Quartier details) {
        Quartier quartier = getQuartierById(id);
        
        // If name changes, check uniqueness
        if (!quartier.getNom().equalsIgnoreCase(details.getNom()) && 
            quartierRepository.existsByNom(details.getNom())) {
            throw new IllegalArgumentException("Quartier name already exists");
        }
        
        quartier.setNom(details.getNom());
        quartier.setTarifLivraison(details.getTarifLivraison());
        return quartierRepository.save(quartier);
    }

    public void deleteQuartier(Long id) {
        Quartier quartier = getQuartierById(id);
        quartierRepository.delete(quartier);
    }
}
