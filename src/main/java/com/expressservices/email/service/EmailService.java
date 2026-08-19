package com.expressservices.email.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class EmailService {

    @Value("${brevo.api-key:}")
    private String apiKey;

    @Value("${brevo.sender-email:expressesrvc@gmail.com}")
    private String senderEmail;

    @Value("${brevo.sender-name:Express Services}")
    private String senderName;

    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";
    private final RestTemplate restTemplate;

    public EmailService() {
        this.restTemplate = new RestTemplate();
    }

    public void sendOtpEmail(String recipientEmail, String recipientName, String username, String initialPassword, String otpCode) {
        // 1. Simulation console en local
        String textBody = "Bonjour " + (recipientName != null ? recipientName : "") + ",\n\n" +
                "Votre compte Express Services a été créé avec succès.\n" +
                " - Nom d'utilisateur : " + username + "\n" +
                " - Mot de passe temporaire : " + initialPassword + "\n\n" +
                " ===> CODE OTP : " + otpCode + " <===\n\n" +
                "Ce code est valide pendant 10 minutes.";

        System.out.println("\n========================================================================\n" +
                "                  [EMAIL SIMULATION] - EXPRESS SERVICES\n" +
                "========================================================================\n" +
                "Destinataire : " + recipientEmail + "\n" +
                "Sujet        : Bienvenue chez Express Services - Vos Identifiants & Code OTP\n" +
                "------------------------------------------------------------------------\n" +
                textBody + "\n" +
                "========================================================================\n");

        if (apiKey == null || apiKey.trim().isEmpty()) {
            System.err.println("⚠️ Brevo API Key non configurée. E-mail réel non envoyé.");
            return;
        }

        // 2. Construction du mail HTML élégant
        String htmlContent = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 25px; border: 1px solid #e2e8f0; border-radius: 12px; background-color: #ffffff;\">" +
                "<div style=\"text-align: center; padding-bottom: 20px; border-bottom: 2px solid #3182ce;\">" +
                "<h2 style=\"color: #2b6cb0; margin: 0;\">🚀 Express Services</h2>" +
                "<p style=\"color: #718096; font-size: 14px; margin-top: 5px;\">Plateforme de Gestion des Livraisons</p>" +
                "</div>" +
                "<div style=\"padding: 20px 0;\">" +
                "<p style=\"font-size: 16px; color: #2d3748;\">Bonjour <strong>" + (recipientName != null ? recipientName : "") + "</strong>,</p>" +
                "<p style=\"font-size: 15px; color: #4a5568;\">Votre compte a été créé avec succès. Voici vos identifiants temporaires :</p>" +
                "<div style=\"background-color: #edf2f7; padding: 15px; border-radius: 8px; margin: 15px 0;\">" +
                "<p style=\"margin: 5px 0; font-size: 14px; color: #2d3748;\">👤 <strong>Nom d'utilisateur :</strong> " + username + "</p>" +
                "<p style=\"margin: 5px 0; font-size: 14px; color: #2d3748;\">🔑 <strong>Mot de passe temporaire :</strong> " + initialPassword + "</p>" +
                "</div>" +
                "<p style=\"font-size: 15px; color: #4a5568;\">Veuillez valider votre première connexion avec le code de sécurité OTP ci-dessous :</p>" +
                "<div style=\"text-align: center; margin: 25px 0;\">" +
                "<span style=\"display: inline-block; background-color: #3182ce; color: #ffffff; font-size: 32px; font-weight: bold; letter-spacing: 6px; padding: 12px 28px; border-radius: 8px;\">" + otpCode + "</span>" +
                "</div>" +
                "<p style=\"font-size: 13px; color: #e53e3e; text-align: center;\">⏱️ Ce code expire dans 10 minutes.</p>" +
                "</div>" +
                "<div style=\"text-align: center; padding-top: 20px; border-top: 1px solid #e2e8f0; font-size: 12px; color: #a0aec0;\">" +
                "<p>© Express Services - Tous droits réservés.</p>" +
                "</div>" +
                "</div>";

        // 3. Envoi via Brevo REST API v3
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey.trim());
            headers.set("accept", "application/json");

            Map<String, Object> body = new HashMap<>();
            
            Map<String, String> sender = new HashMap<>();
            sender.put("name", senderName);
            sender.put("email", senderEmail);
            body.put("sender", sender);

            List<Map<String, String>> toList = new ArrayList<>();
            Map<String, String> toUser = new HashMap<>();
            toUser.put("email", recipientEmail);
            if (recipientName != null && !recipientName.isEmpty()) {
                toUser.put("name", recipientName);
            }
            toList.add(toUser);
            body.put("to", toList);

            body.put("subject", "🔑 Code de Sécurité OTP Express Services : " + otpCode);
            body.put("htmlContent", htmlContent);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(BREVO_API_URL, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("✅ REAL EMAIL SENT SUCCESSFULLY via Brevo REST API v3 to: " + recipientEmail);
            } else {
                System.err.println("⚠️ Échec d'envoi Brevo REST API, Code HTTP : " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de l'e-mail via Brevo REST API : " + e.getMessage());
        }
    }
}
