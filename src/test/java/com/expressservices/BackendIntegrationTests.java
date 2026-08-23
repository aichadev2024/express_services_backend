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

    @Autowired
    private com.expressservices.auth.repository.UserRepository userRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Test
    void testDefaultAdminAndLogin() {
        if (!userRepository.existsByUsername("admin_test")) {
            User admin = User.builder()
                    .username("admin_test")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ROLE_ADMIN)
                    .nom("AdminTest")
                    .prenom("Super")
                    .telephone("+22370000000")
                    .otpVerified(true)
                    .build();
            userRepository.save(admin);
        }
        LoginRequest loginRequest = new LoginRequest("admin_test", "admin123");
        LoginResponse loginResponse = authService.login(loginRequest);
        
        assertNotNull(loginResponse);
        assertEquals("admin_test", loginResponse.getUsername());
        assertEquals("ROLE_ADMIN", loginResponse.getRole());
    }

    @Test
    void testQuartierCRUD() {
        List<Quartier> initial = quartierService.getAllQuartiers();
        assertFalse(initial.isEmpty());
        
        Quartier q = Quartier.builder().nom("Aci 2000").tarifLivraison(1500.0).build();
        Quartier saved = quartierService.createQuartier(q);
        
        assertNotNull(saved.getId());
        assertEquals("Aci 2000", saved.getNom());
        
        saved.setTarifLivraison(1800.0);
        Quartier updated = quartierService.updateQuartier(saved.getId(), saved);
        assertEquals(1800.0, updated.getTarifLivraison());
    }

    @Test
    void testCommandeLifecycleAndDailyStats() {
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
        
        RegisterRequest reg = RegisterRequest.builder()
                .username("driver_bob")
                .password("pass123")
                .role("ROLE_LIVREUR")
                .nom("Bob")
                .prenom("Driver")
                .telephone("+22370001122")
                .build();
        User driver = authService.registerLivreur(reg);
        
        CommandeResponse assigned = commandeService.assignLivreur(res.getId(), driver.getId());
        assertEquals("driver_bob", assigned.getLivreurUsername());
        assertEquals(StatutCommande.EN_COURS.name(), assigned.getStatut());
        
        CommandeResponse updated = commandeService.updateStatus(res.getId(), "LIVREE", "driver_bob");
        assertEquals(StatutCommande.LIVREE.name(), updated.getStatut());
        
        String waLink = commandeService.getWhatsAppLink(res.getId());
        assertTrue(waLink.contains("Moussa+Traor"));
        assertTrue(waLink.contains("22377889900"));

        com.expressservices.commande.dto.DailyDeliveryStatsResponse dailyStats = 
                commandeService.getDailyDeliveryStats(java.time.LocalDate.now());
        assertNotNull(dailyStats);
        assertTrue(dailyStats.getNombreLivraisonsLivrees() >= 1);
        assertTrue(dailyStats.getTotalFraisLivraison().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testRegisterLivreurWithoutEmail_DirectLoginNoOtp() {
        RegisterRequest reg = RegisterRequest.builder()
                .username("driver_no_email_1")
                .email(null)
                .password("driverpass123")
                .role("ROLE_LIVREUR")
                .nom("Coulibaly")
                .prenom("Oumar")
                .telephone("+22370112233")
                .build();
        User user = authService.registerLivreur(reg);

        assertNotNull(user);
        assertEquals("driver_no_email_1", user.getUsername());
        assertNull(user.getEmail());
        assertTrue(user.isOtpVerified()); // Auto-verified when no email

        LoginRequest loginRequest = new LoginRequest("driver_no_email_1", "driverpass123");
        LoginResponse loginRes = authService.login(loginRequest);

        assertNotNull(loginRes);
        assertFalse(loginRes.isOtpRequired()); // No OTP required!
        assertNotNull(loginRes.getToken());
    }
}
