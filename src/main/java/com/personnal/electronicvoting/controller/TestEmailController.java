package com.personnal.electronicvoting.controller;

import com.personnal.electronicvoting.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
public class TestEmailController {

    private final EmailService emailService;

    /**
     * 🧪 Test rapide d'envoi d'email
     * GET http://localhost:8080/api/test/email?destination=votre-email@gmail.com
     */
    @GetMapping("/email")
    public String testerEmail(@RequestParam String destination) {
        log.info("🧪 Test email demandé pour: {}", destination);

        try {
            emailService.testerEmail(destination);
            return "✅ Email de test envoyé avec succès à: " + destination;
        } catch (Exception e) {
            log.error("💥 Erreur test email: {}", e.getMessage(), e);
            return "❌ Erreur envoi email: " + e.getMessage();
        }
    }

    /**
     * 🧪 Test envoi identifiants électeur
     * GET http://localhost:8080/api/test/identifiants?email=test@gmail.com&username=testuser
     */
    @GetMapping("/identifiants")
    public String testerIdentifiants(
            @RequestParam String email,
            @RequestParam String username) {

        log.info("🧪 Test identifiants pour: {} ({})", username, email);

        try {
            String motDePasseTest = "TempPass123!";
            emailService.envoyerIdentifiantsElecteur(email, username, motDePasseTest);
            return String.format("✅ Identifiants envoyés à %s (%s) avec mot de passe: %s",
                    username, email, motDePasseTest);
        } catch (Exception e) {
            log.error("💥 Erreur test identifiants: {}", e.getMessage(), e);
            return "❌ Erreur envoi identifiants: " + e.getMessage();
        }
    }
}