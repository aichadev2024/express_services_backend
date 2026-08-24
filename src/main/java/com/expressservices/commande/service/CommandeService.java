package com.expressservices.commande.service;

import com.expressservices.auth.model.Role;
import com.expressservices.auth.model.User;
import com.expressservices.auth.service.AuthService;
import com.expressservices.commande.dto.*;
import com.expressservices.commande.model.*;
import com.expressservices.commande.repository.CommandeRepository;
import com.expressservices.exception.ResourceNotFoundException;
import com.expressservices.partenaire.service.PartenaireService;
import com.expressservices.partenaire.model.Partenaire;
import com.expressservices.produit.model.Produit;
import com.expressservices.produit.service.ProduitService;
import com.expressservices.quartier.model.Quartier;
import com.expressservices.quartier.service.QuartierService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CommandeService {

    private final CommandeRepository commandeRepository;
    private final QuartierService quartierService;
    private final AuthService authService;
    private final ProduitService produitService;
    private final PartenaireService partenaireService;

    public CommandeService(CommandeRepository commandeRepository, QuartierService quartierService,
                           AuthService authService, ProduitService produitService,
                           PartenaireService partenaireService) {
        this.commandeRepository = commandeRepository;
        this.quartierService = quartierService;
        this.authService = authService;
        this.produitService = produitService;
        this.partenaireService = partenaireService;
    }

    public CommandeResponse createCommande(CommandeRequest request) {
        Quartier quartier = quartierService.getQuartierById(request.getQuartierId());

        Partenaire partenaire = request.getPartenaireId() != null
                ? partenaireService.getPartenaireEntityById(request.getPartenaireId())
                : null;

        Commande commande = Commande.builder()
                .nomClient(request.getNomClient())
                .telephoneClient(request.getTelephoneClient())
                .emailClient(request.getEmailClient())
                .quartier(quartier)
                .adressePrecise(request.getAdressePrecise())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .dateHeureSouhaitee(request.getDateHeureSouhaitee())
                .statut(StatutCommande.EN_ATTENTE)
                .partenaire(partenaire)
                .descriptionArticle(request.getDescriptionArticle())
                .livraisonGratuite(Boolean.TRUE.equals(request.getLivraisonGratuite()))
                .build();

        // Résoudre les produits et créer les lignes
        List<CommandeProduit> lignes = new ArrayList<>();
        if (request.getLignesProduits() != null) {
            for (LigneProduitRequest ligneReq : request.getLignesProduits()) {
                Produit produit = produitService.getProduitEntityById(ligneReq.getProduitId());

                validateProduitAppartientAuPartenaire(produit, partenaire);
                produitService.decrementStock(produit.getId(), ligneReq.getQuantite());

                BigDecimal prixEffectif = (ligneReq.getPrixUnitaire() != null && ligneReq.getPrixUnitaire().compareTo(BigDecimal.ZERO) >= 0)
                        ? ligneReq.getPrixUnitaire()
                        : produit.getPrix();

                CommandeProduit ligne = CommandeProduit.builder()
                        .commande(commande)
                        .produit(produit)
                        .quantite(ligneReq.getQuantite())
                        .prixUnitaire(prixEffectif)
                        .build();
                lignes.add(ligne);
            }
        }
        commande.getLignesProduits().addAll(lignes);

        Commande saved = commandeRepository.save(commande);
        return mapToResponse(saved);
    }

    /**
     * Regle #9 de la conception : si la commande a un partenaire, ses lignes ne peuvent
     * referencer que des produits appartenant a ce partenaire.
     */
    private void validateProduitAppartientAuPartenaire(Produit produit, Partenaire partenaire) {
        if (partenaire == null) {
            return;
        }
        if (produit.getPartenaire() == null || !produit.getPartenaire().getId().equals(partenaire.getId())) {
            throw new IllegalArgumentException(
                    "Le produit " + produit.getNom() + " n'appartient pas au partenaire " + partenaire.getNom());
        }
    }

    public CommandeResponse updateCommande(Long id, CommandeRequest request) {
        Commande commande = commandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande introuvable avec ID: " + id));

        Quartier quartier = quartierService.getQuartierById(request.getQuartierId());

        Partenaire partenaire = request.getPartenaireId() != null
                ? partenaireService.getPartenaireEntityById(request.getPartenaireId())
                : null;

        boolean active = isStateActive(commande.getStatut());
        if (active) {
            // Restore stock for old lines
            for (CommandeProduit ligne : commande.getLignesProduits()) {
                produitService.restoreStock(ligne.getProduit().getId(), ligne.getQuantite());
            }
        }

        commande.setNomClient(request.getNomClient());
        commande.setTelephoneClient(request.getTelephoneClient());
        commande.setEmailClient(request.getEmailClient());
        commande.setQuartier(quartier);
        commande.setAdressePrecise(request.getAdressePrecise());
        commande.setLatitude(request.getLatitude());
        commande.setLongitude(request.getLongitude());
        commande.setDateHeureSouhaitee(request.getDateHeureSouhaitee());
        commande.setPartenaire(partenaire);
        if (request.getLivraisonGratuite() != null) {
            commande.setLivraisonGratuite(request.getLivraisonGratuite());
        }
        commande.setDescriptionArticle(request.getDescriptionArticle());

        // Remplace entièrement les lignes de produits (orphanRemoval supprime les anciennes)
        commande.getLignesProduits().clear();
        if (request.getLignesProduits() != null) {
            for (LigneProduitRequest ligneReq : request.getLignesProduits()) {
                Produit produit = produitService.getProduitEntityById(ligneReq.getProduitId());

                validateProduitAppartientAuPartenaire(produit, partenaire);

                if (active) {
                    produitService.decrementStock(produit.getId(), ligneReq.getQuantite());
                }

                BigDecimal prixEffectif = (ligneReq.getPrixUnitaire() != null && ligneReq.getPrixUnitaire().compareTo(BigDecimal.ZERO) >= 0)
                        ? ligneReq.getPrixUnitaire()
                        : produit.getPrix();

                CommandeProduit ligne = CommandeProduit.builder()
                        .commande(commande)
                        .produit(produit)
                        .quantite(ligneReq.getQuantite())
                        .prixUnitaire(prixEffectif)
                        .build();
                commande.getLignesProduits().add(ligne);
            }
        }

        return mapToResponse(commandeRepository.save(commande));
    }

    @Transactional(readOnly = true)
    public List<CommandeResponse> getAllCommandes(String statusStr, Long livreurId) {
        return getAllCommandes(statusStr, livreurId, null);
    }

    @Transactional(readOnly = true)
    public List<CommandeResponse> getAllCommandes(String statusStr, Long livreurId, java.time.LocalDate date) {
        List<Commande> list;

        StatutCommande status = null;
        if (statusStr != null && !statusStr.trim().isEmpty()) {
            try {
                status = StatutCommande.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        if (date != null) {
            java.time.LocalDateTime start = date.atStartOfDay();
            java.time.LocalDateTime end = date.atTime(23, 59, 59, 999999999);

            if (livreurId != null && status != null) {
                list = commandeRepository.findByLivreurIdAndStatutAndDateCreationBetweenOrderByDateCreationDesc(livreurId, status, start, end);
            } else if (livreurId != null) {
                list = commandeRepository.findByLivreurIdAndDateCreationBetweenOrderByDateCreationDesc(livreurId, start, end);
            } else if (status != null) {
                list = commandeRepository.findByStatutAndDateCreationBetweenOrderByDateCreationDesc(status, start, end);
            } else {
                list = commandeRepository.findByDateCreationBetweenOrderByDateCreationDesc(start, end);
            }
        } else {
            if (livreurId != null && status != null) {
                list = commandeRepository.findByLivreurIdAndStatutOrderByDateCreationDesc(livreurId, status);
            } else if (livreurId != null) {
                list = commandeRepository.findByLivreurIdOrderByDateCreationDesc(livreurId);
            } else if (status != null) {
                list = commandeRepository.findByStatutOrderByDateCreationDesc(status);
            } else {
                list = commandeRepository.findAllByOrderByDateCreationDesc();
            }
        }

        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CommandeResponse getCommandeById(Long id) {
        Commande commande = commandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande introuvable avec ID: " + id));
        return mapToResponse(commande);
    }

    public CommandeResponse assignLivreur(Long id, Long livreurId) {
        Commande commande = commandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande introuvable avec ID: " + id));

        if (livreurId == null) {
            commande.setLivreur(null);
        } else {
            User livreur = authService.getUserEntityById(livreurId);
            if (livreur.getRole() != Role.ROLE_LIVREUR) {
                throw new IllegalArgumentException("L'utilisateur assigné doit avoir le rôle ROLE_LIVREUR");
            }
            commande.setLivreur(livreur);
            // Passage automatique à EN_COURS quand un livreur est assigné
            if (commande.getStatut() == StatutCommande.EN_ATTENTE) {
                commande.setStatut(StatutCommande.EN_COURS);
            }
        }

        return mapToResponse(commandeRepository.save(commande));
    }

    public CommandeResponse updateStatus(Long id, String statutStr, String usernameFromAuth) {
        return updateStatus(id, statutStr, null, usernameFromAuth);
    }

    public CommandeResponse updateStatus(Long id, String statutStr, String motif, String usernameFromAuth) {
        Commande commande = commandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande introuvable avec ID: " + id));

        User currentUser = authService.getUserEntityByUsername(usernameFromAuth);

        // Contrôle d'accès : un livreur ne peut modifier que les commandes qui lui sont assignées
        if (currentUser.getRole() == Role.ROLE_LIVREUR) {
            if (commande.getLivreur() == null || !commande.getLivreur().getId().equals(currentUser.getId())) {
                throw new SecurityException("Accès refusé : vous ne pouvez modifier que vos propres commandes.");
            }
        }

        StatutCommande newStatus;
        try {
            newStatus = StatutCommande.valueOf(statutStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Statut invalide : " + statutStr);
        }

        StatutCommande oldStatus = commande.getStatut();
        commande.setStatut(newStatus);
        if (motif != null && !motif.trim().isEmpty()) {
            commande.setMotifAnnulation(motif.trim());
        }
        adjustStockOnStatusChange(commande, oldStatus, newStatus);

        return mapToResponse(commandeRepository.save(commande));
    }

    @Transactional(readOnly = true)
    public String getWhatsAppLink(Long id) {
        Commande commande = commandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande introuvable avec ID: " + id));

        String phone = commande.getTelephoneClient().replaceAll("[^0-9+]", "");

        String produitsResume = commande.getLignesProduits().stream()
                .map(l -> l.getQuantite() + "x " + l.getProduit().getNom())
                .collect(Collectors.joining(", "));

        String message = String.format(
                "Bonjour %s, votre commande (%s) pour le quartier %s est actuellement : %s.",
                commande.getNomClient(),
                produitsResume,
                commande.getQuartier().getNom(),
                commande.getStatut().name()
        );

        String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);
        return "https://api.whatsapp.com/send?phone=" + phone + "&text=" + encodedMessage;
    }

    private CommandeResponse mapToResponse(Commande commande) {
        List<LigneProduitResponse> lignesDto = commande.getLignesProduits().stream()
                .map(l -> LigneProduitResponse.builder()
                        .produitId(l.getProduit().getId())
                        .produitNom(l.getProduit().getNom())
                        .quantite(l.getQuantite())
                        .prixUnitaire(l.getPrixUnitaire())
                        .sousTotal(l.getSousTotal())
                        .build())
                .collect(Collectors.toList());

        return CommandeResponse.builder()
                .id(commande.getId())
                .nomClient(commande.getNomClient())
                .telephoneClient(commande.getTelephoneClient())
                .emailClient(commande.getEmailClient())
                .lignesProduits(lignesDto)
                .quartierId(commande.getQuartier().getId())
                .quartierNom(commande.getQuartier().getNom())
                .tarifLivraison(commande.getQuartier().getTarifLivraison())
                .adressePrecise(commande.getAdressePrecise())
                .latitude(commande.getLatitude())
                .longitude(commande.getLongitude())
                .dateHeureSouhaitee(commande.getDateHeureSouhaitee())
                .statut(commande.getStatut().name())
                .dateCreation(commande.getDateCreation())
                .livreurId(commande.getLivreur() != null ? commande.getLivreur().getId() : null)
                .livreurUsername(commande.getLivreur() != null ? commande.getLivreur().getUsername() : null)
                .livreurNom(commande.getLivreur() != null ? commande.getLivreur().getNom() : null)
                .livreurPrenom(commande.getLivreur() != null ? commande.getLivreur().getPrenom() : null)
                .montantProduits(commande.getMontantProduits())
                .montantTotal(commande.getMontantTotal())
                .partenaireId(commande.getPartenaire() != null ? commande.getPartenaire().getId() : null)
                .partenaireNom(commande.getPartenaire() != null ? commande.getPartenaire().getNom() : null)
                .descriptionArticle(commande.getDescriptionArticle())
                .motifAnnulation(commande.getMotifAnnulation())
                .livraisonGratuite(commande.getLivraisonGratuite())
                .tarifLivraisonEffective(commande.getTarifLivraisonEffective() != null ? commande.getTarifLivraisonEffective().doubleValue() : 0.0)
                .montantAEncaisser(commande.getMontantTotal())
                .build();
    }

    private boolean isStateActive(StatutCommande state) {
        return state == StatutCommande.EN_ATTENTE || 
               state == StatutCommande.EN_COURS || 
               state == StatutCommande.LIVREE;
    }

    private void adjustStockOnStatusChange(Commande commande, StatutCommande oldStatus, StatutCommande newStatus) {
        boolean oldActive = isStateActive(oldStatus);
        boolean newActive = isStateActive(newStatus);

        if (oldActive && !newActive) {
            // Restore stock (order cancelled, rejected, postponed, or injoignable)
            for (CommandeProduit ligne : commande.getLignesProduits()) {
                produitService.restoreStock(ligne.getProduit().getId(), ligne.getQuantite());
            }
        } else if (!oldActive && newActive) {
            // Re-reserve stock
            for (CommandeProduit ligne : commande.getLignesProduits()) {
                produitService.decrementStock(ligne.getProduit().getId(), ligne.getQuantite());
            }
        }
    }

    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        java.time.LocalDateTime startOfDay = java.time.LocalDate.now().atStartOfDay();
        List<Commande> todayOrders = commandeRepository.findByDateCreationAfter(startOfDay);

        long totalToday = todayOrders.size();
        long pending = commandeRepository.countByStatut(StatutCommande.EN_ATTENTE);
        long enCours = commandeRepository.countByStatut(StatutCommande.EN_COURS);
        long livree = commandeRepository.countByStatut(StatutCommande.LIVREE);
        long annulee = commandeRepository.countByStatut(StatutCommande.ANNULEE);
        long injoignable = commandeRepository.countByStatut(StatutCommande.INJOIGNABLE);
        long reportee = commandeRepository.countByStatut(StatutCommande.REPORTEE);
        long rejetees = commandeRepository.countByStatut(StatutCommande.REJETEE);

        BigDecimal totalAmountToday = todayOrders.stream()
                .filter(c -> c.getStatut() != StatutCommande.ANNULEE && c.getStatut() != StatutCommande.REJETEE)
                .map(Commande::getMontantTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return DashboardStatsResponse.builder()
                .totalCommandesDuJour(totalToday)
                .commandesEnAttente(pending)
                .commandesEnCours(enCours)
                .commandesLivrees(livree)
                .commandesAnnulees(annulee)
                .commandesRejetees(rejetees)
                .commandesReportees(reportee)
                .commandesInjoignables(injoignable)
                .montantTotalDuJour(totalAmountToday)
                .build();
    }

    @Transactional(readOnly = true)
    public List<CommandeResponse> trackCommandes(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String finalQuery = query.trim();
        List<Commande> list = new ArrayList<>();

        // Try parsing as ID
        try {
            Long id = Long.parseLong(finalQuery);
            commandeRepository.findById(id).ifPresent(list::add);
        } catch (NumberFormatException ignored) {}

        // Add phone matches
        for (Commande c : commandeRepository.findByTelephoneClientContaining(finalQuery)) {
            if (list.stream().noneMatch(o -> o.getId().equals(c.getId()))) {
                list.add(c);
            }
        }

        // Add name matches (destinataire)
        for (Commande c : commandeRepository.findByNomClientContainingIgnoreCase(finalQuery)) {
            if (list.stream().noneMatch(o -> o.getId().equals(c.getId()))) {
                list.add(c);
            }
        }

        // Add partner name matches
        for (Commande c : commandeRepository.findByPartenaireNomContainingIgnoreCase(finalQuery)) {
            if (list.stream().noneMatch(o -> o.getId().equals(c.getId()))) {
                list.add(c);
            }
        }

        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public java.util.Map<String, Object> getPublicStats() {
        long delivered = commandeRepository.countByStatut(StatutCommande.LIVREE);
        long activeDeliveries = commandeRepository.countByStatut(StatutCommande.EN_COURS);
        long partners = partenaireService.getAllPartenaires().size();
        long cancelled = commandeRepository.countByStatut(StatutCommande.ANNULEE);
        long rejected = commandeRepository.countByStatut(StatutCommande.REJETEE);

        double satisfaction = 99.2; // Valeur par défaut réaliste
        long totalClosed = delivered + cancelled + rejected;
        if (totalClosed > 0) {
            satisfaction = ((double) delivered / totalClosed) * 100;
        }

        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("colisLivres", delivered);
        stats.put("livraisonsEnCours", activeDeliveries);
        stats.put("partenaires", partners);
        stats.put("satisfaction", satisfaction);
        return stats;
    }

    @Transactional(readOnly = true)
    public DailyDeliveryStatsResponse getDailyDeliveryStats(java.time.LocalDate targetDate) {
        java.time.LocalDate date = targetDate != null ? targetDate : java.time.LocalDate.now();
        java.time.LocalDateTime startOfDay = date.atStartOfDay();
        java.time.LocalDateTime endOfDay = date.atTime(23, 59, 59, 999999999);

        List<Commande> dayOrders = commandeRepository.findByDateCreationBetweenOrderByDateCreationDesc(startOfDay, endOfDay);
        List<Commande> deliveredOrders = dayOrders.stream()
                .filter(c -> c.getStatut() == StatutCommande.LIVREE)
                .collect(Collectors.toList());

        long totalLivraisonsDuJour = dayOrders.size();
        long nombreLivraisonsLivrees = deliveredOrders.size();
        long totalLivraisonsGratuites = deliveredOrders.stream()
                .filter(c -> Boolean.TRUE.equals(c.getLivraisonGratuite()))
                .count();

        // 0 FCFA delivery fee counted if livraisonGratuite is true
        BigDecimal totalFraisLivraison = deliveredOrders.stream()
                .map(c -> Boolean.TRUE.equals(c.getLivraisonGratuite())
                        ? BigDecimal.ZERO
                        : (c.getQuartier() != null ? BigDecimal.valueOf(c.getQuartier().getTarifLivraison()) : BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalMontantMarchandises = deliveredOrders.stream()
                .map(Commande::getMontantProduits)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalMontantGlobal = deliveredOrders.stream()
                .map(Commande::getMontantTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<User> drivers = authService.getAllLivreurs();
        List<LivreurDailyStatDto> livreursStats = drivers.stream().map(driver -> {
            List<Commande> driverDayOrders = dayOrders.stream()
                    .filter(c -> c.getLivreur() != null && c.getLivreur().getId().equals(driver.getId()))
                    .collect(Collectors.toList());

            List<Commande> driverDeliveredOrders = driverDayOrders.stream()
                    .filter(c -> c.getStatut() == StatutCommande.LIVREE)
                    .collect(Collectors.toList());

            long driverGratuites = driverDeliveredOrders.stream()
                    .filter(c -> Boolean.TRUE.equals(c.getLivraisonGratuite()))
                    .count();

            BigDecimal driverFrais = driverDeliveredOrders.stream()
                    .map(c -> Boolean.TRUE.equals(c.getLivraisonGratuite())
                            ? BigDecimal.ZERO
                            : (c.getQuartier() != null ? BigDecimal.valueOf(c.getQuartier().getTarifLivraison()) : BigDecimal.ZERO))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal driverMarchandises = driverDeliveredOrders.stream()
                    .map(Commande::getMontantProduits)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal driverGlobal = driverDeliveredOrders.stream()
                    .map(Commande::getMontantTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            return LivreurDailyStatDto.builder()
                    .livreurId(driver.getId())
                    .livreurUsername(driver.getUsername())
                    .livreurNom(driver.getNom())
                    .livreurPrenom(driver.getPrenom())
                    .livreurTelephone(driver.getTelephone())
                    .nombreLivraisonsAssignees(driverDayOrders.size())
                    .nombreLivraisonsLivrees(driverDeliveredOrders.size())
                    .totalLivraisonsGratuites(driverGratuites)
                    .totalFraisLivraison(driverFrais)
                    .totalMontantMarchandises(driverMarchandises)
                    .totalMontantGlobal(driverGlobal)
                    .build();
        }).collect(Collectors.toList());

        List<DailyHistoryStatDto> historique7Jours = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            java.time.LocalDate hDate = date.minusDays(i);
            java.time.LocalDateTime hStart = hDate.atStartOfDay();
            java.time.LocalDateTime hEnd = hDate.atTime(23, 59, 59, 999999999);

            List<Commande> hOrders = commandeRepository.findByDateCreationBetweenOrderByDateCreationDesc(hStart, hEnd);
            List<Commande> hDelivered = hOrders.stream()
                    .filter(c -> c.getStatut() == StatutCommande.LIVREE)
                    .collect(Collectors.toList());

            long hGratuites = hDelivered.stream()
                    .filter(c -> Boolean.TRUE.equals(c.getLivraisonGratuite()))
                    .count();

            BigDecimal hFrais = hDelivered.stream()
                    .map(c -> Boolean.TRUE.equals(c.getLivraisonGratuite())
                            ? BigDecimal.ZERO
                            : (c.getQuartier() != null ? BigDecimal.valueOf(c.getQuartier().getTarifLivraison()) : BigDecimal.ZERO))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal hMarchandises = hDelivered.stream()
                    .map(Commande::getMontantProduits)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal hGlobal = hDelivered.stream()
                    .map(Commande::getMontantTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            historique7Jours.add(DailyHistoryStatDto.builder()
                    .date(hDate)
                    .totalLivraisons(hOrders.size())
                    .totalCommandesLivrees(hDelivered.size())
                    .totalLivraisonsGratuites(hGratuites)
                    .totalFraisLivraison(hFrais)
                    .totalMontantMarchandises(hMarchandises)
                    .totalMontantGlobal(hGlobal)
                    .build());
        }

        return DailyDeliveryStatsResponse.builder()
                .date(date)
                .totalLivraisonsDuJour(totalLivraisonsDuJour)
                .nombreLivraisonsLivrees(nombreLivraisonsLivrees)
                .totalLivraisonsGratuites(totalLivraisonsGratuites)
                .totalFraisLivraison(totalFraisLivraison)
                .totalMontantMarchandises(totalMontantMarchandises)
                .totalMontantGlobal(totalMontantGlobal)
                .livreursStats(livreursStats)
                .historique7Jours(historique7Jours)
                .build();
    }
}
