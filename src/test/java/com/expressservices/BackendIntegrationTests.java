package com.expressservices;

import com.expressservices.auth.dto.LoginRequest;
import com.expressservices.auth.dto.LoginResponse;
import com.expressservices.auth.dto.RegisterRequest;
import com.expressservices.auth.model.Role;
import com.expressservices.auth.model.User;
import com.expressservices.auth.service.AuthService;
import com.expressservices.commande.dto.CommandeRequest;
import com.expressservices.commande.dto.CommandeResponse;
import com.expressservices.commande.dto.LigneProduitRequest;
import com.expressservices.commande.model.StatutCommande;
import com.expressservices.commande.service.CommandeService;
import com.expressservices.produit.dto.ProduitRequest;
import com.expressservices.produit.dto.ProduitResponse;
import com.expressservices.produit.service.ProduitService;
import com.expressservices.quartier.model.Quartier;
import com.expressservices.quartier.service.QuartierService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class BackendIntegrationTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private QuartierService quartierService;

    @Autowired
    private CommandeService commandeService;

    @Autowired
    private ProduitService produitService;

    @Test
    void testDefaultAdminAndLogin() {
        // Default admin should have been initialized via @PostConstruct in AuthService
        LoginRequest loginRequest = new LoginRequest("admin", "admin123");
        LoginResponse loginResponse = authService.login(loginRequest);
        
        assertNotNull(loginResponse);
        assertNotNull(loginResponse.getToken());
        assertEquals("admin", loginResponse.getUsername());
        assertEquals("ROLE_ADMIN", loginResponse.getRole());
    }

    @Test
    void testRegisterLivreur() {
        RegisterRequest reg = new RegisterRequest("john_driver", "securepass123", "ROLE_LIVREUR", "Traoré", "John", null);
        User user = authService.registerLivreur(reg);
        
        assertNotNull(user);
        assertEquals("john_driver", user.getUsername());
        assertEquals(Role.ROLE_LIVREUR, user.getRole());
        
        // Try logging in
        LoginRequest loginRequest = new LoginRequest("john_driver", "securepass123");
        LoginResponse loginResponse = authService.login(loginRequest);
        
        assertNotNull(loginResponse);
        assertEquals("ROLE_LIVREUR", loginResponse.getRole());
    }

    @Test
    void testQuartierCRUD() {
        List<Quartier> initial = quartierService.getAllQuartiers();
        assertFalse(initial.isEmpty()); // Should have seeded values
        
        Quartier q = Quartier.builder().nom("Aci 2000").tarifLivraison(1500.0).build();
        Quartier saved = quartierService.createQuartier(q);
        
        assertNotNull(saved.getId());
        assertEquals("Aci 2000", saved.getNom());
        
        // Update
        saved.setTarifLivraison(1800.0);
        Quartier updated = quartierService.updateQuartier(saved.getId(), saved);
        assertEquals(1800.0, updated.getTarifLivraison());
    }

    @Test
    void testCommandeLifecycle() {
        // Retrieve seeded neighborhood (e.g. Baguineda)
        List<Quartier> quartiers = quartierService.getAllQuartiers();
        Quartier q = quartiers.get(0);
        
        ProduitResponse p = produitService.createProduit(new ProduitRequest(
                "Sac de riz 25kg", BigDecimal.valueOf(15000), null, 10, true, null));

        CommandeRequest req = CommandeRequest.builder()
                .nomClient("Moussa Traoré")
                .telephoneClient("+22377889900")
                .emailClient("moussa@example.com")
                .lignesProduits(List.of(new LigneProduitRequest(p.getId(), 2)))
                .quartierId(q.getId())
                .adressePrecise("Près du marché de Baguineda")
                .dateHeureSouhaitee(LocalDateTime.now().plusDays(1))
                .build();
                
        CommandeResponse res = commandeService.createCommande(req);
        
        assertNotNull(res);
        assertNotNull(res.getId());
        assertEquals("Moussa Traoré", res.getNomClient());
        assertEquals(StatutCommande.EN_ATTENTE.name(), res.getStatut());
        assertNull(res.getLivreurId());
        
        // Register a driver and assign
        RegisterRequest reg = new RegisterRequest("driver_bob", "pass123", "ROLE_LIVREUR", "Bob", "Driver", null);
        User driver = authService.registerLivreur(reg);
        
        CommandeResponse assigned = commandeService.assignLivreur(res.getId(), driver.getId());
        assertEquals("driver_bob", assigned.getLivreurUsername());
        // Status should automatically change to EN_COURS
        assertEquals(StatutCommande.EN_COURS.name(), assigned.getStatut());
        
        // Update status as driver
        CommandeResponse updated = commandeService.updateStatus(res.getId(), "LIVREE", "driver_bob");
        assertEquals(StatutCommande.LIVREE.name(), updated.getStatut());
        
        // Verify manual WhatsApp message generation link
        String waLink = commandeService.getWhatsAppLink(res.getId());
        assertTrue(waLink.contains("Moussa+Traor"));
        assertTrue(waLink.contains("22377889900"));
    }
}
