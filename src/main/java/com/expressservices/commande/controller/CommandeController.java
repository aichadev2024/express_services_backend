package com.expressservices.commande.controller;

import com.expressservices.auth.model.Role;
import com.expressservices.auth.model.User;
import com.expressservices.auth.service.AuthService;
import com.expressservices.commande.dto.CommandeRequest;
import com.expressservices.commande.dto.CommandeResponse;
import com.expressservices.commande.dto.StatusUpdateRequest;
import com.expressservices.commande.dto.DashboardStatsResponse;
import com.expressservices.commande.service.CommandeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/commandes")
public class CommandeController {

    private final CommandeService commandeService;
    private final AuthService authService;

    public CommandeController(CommandeService commandeService, AuthService authService) {
        this.commandeService = commandeService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<CommandeResponse> createCommande(@RequestBody CommandeRequest request) {
        CommandeResponse response = commandeService.createCommande(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/track")
    public ResponseEntity<List<CommandeResponse>> trackCommandes(@RequestParam String query) {
        return ResponseEntity.ok(commandeService.trackCommandes(query));
    }

    @GetMapping("/public-stats")
    public ResponseEntity<Map<String, Object>> getPublicStats() {
        return ResponseEntity.ok(commandeService.getPublicStats());
    }

    @GetMapping
    public ResponseEntity<List<CommandeResponse>> getAllCommandes(
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) Long livreurId,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate date,
            Authentication authentication) {

        User currentUser = authService.getUserEntityByUsername(authentication.getName());

        // Security check: Drivers (Livreur) can only see their own assigned orders
        if (currentUser.getRole() == Role.ROLE_LIVREUR) {
            return ResponseEntity.ok(commandeService.getAllCommandes(statut, currentUser.getId(), date));
        }

        // Admin can view all or filter freely
        return ResponseEntity.ok(commandeService.getAllCommandes(statut, livreurId, date));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommandeResponse> getCommandeById(@PathVariable Long id, Authentication authentication) {
        CommandeResponse response = commandeService.getCommandeById(id);

        User currentUser = authService.getUserEntityByUsername(authentication.getName());

        // Security check: Drivers can only view detailed info if the order is assigned to them
        if (currentUser.getRole() == Role.ROLE_LIVREUR) {
            if (response.getLivreurId() == null || !response.getLivreurId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<CommandeResponse> updateCommande(
            @PathVariable Long id,
            @RequestBody CommandeRequest request) {
        CommandeResponse response = commandeService.updateCommande(id, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<CommandeResponse> assignLivreur(
            @PathVariable Long id,
            @RequestBody Map<String, Long> payload) {
        
        Long livreurId = payload.get("livreurId");
        CommandeResponse response = commandeService.assignLivreur(id, livreurId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<CommandeResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody StatusUpdateRequest request,
            Authentication authentication) {

        CommandeResponse response = commandeService.updateStatus(id, request.getStatut(), request.getMotif(), authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/whatsapp")
    public ResponseEntity<Map<String, String>> getWhatsAppLink(@PathVariable Long id, Authentication authentication) {
        CommandeResponse response = commandeService.getCommandeById(id);
        
        User currentUser = authService.getUserEntityByUsername(authentication.getName());

        // Security check: Drivers can only get the message link for orders assigned to them
        if (currentUser.getRole() == Role.ROLE_LIVREUR) {
            if (response.getLivreurId() == null || !response.getLivreurId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        String link = commandeService.getWhatsAppLink(id);
        return ResponseEntity.ok(Map.of("link", link));
    }

    @GetMapping("/dashboard-stats")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        return ResponseEntity.ok(commandeService.getDashboardStats());
    }

    @GetMapping("/daily-stats")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<com.expressservices.commande.dto.DailyDeliveryStatsResponse> getDailyDeliveryStats(
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate date) {
        return ResponseEntity.ok(commandeService.getDailyDeliveryStats(date));
    }

    @GetMapping("/monthly-stats")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<com.expressservices.commande.dto.MonthlyDeliveryStatsResponse> getMonthlyDeliveryStats(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(commandeService.getMonthlyDeliveryStats(year, month));
    }
}
