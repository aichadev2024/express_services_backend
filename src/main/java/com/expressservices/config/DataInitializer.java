package com.expressservices.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Override
    public void run(String... args) {
        // Aucune donnée n'est créée automatiquement.
        // La gestion des données se fait intégralement via l'interface web.
    }
}
