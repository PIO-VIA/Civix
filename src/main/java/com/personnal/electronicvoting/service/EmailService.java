
package com.personnal.electronicvoting.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * Envoyer identifiants à un nouvel électeur
     */
    public void envoyerIdentifiantsElecteur(String email, String username, String motDePasseTemporaire) {
        log.info(" Envoi identifiants électeur à: {}", email);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setFrom("noreply@platformevote.com"); // ← Votre email
            message.setSubject(" Vos identifiants pour l'élection");
            message.setText(construireMessageIdentifiants(username, email, motDePasseTemporaire));

            mailSender.send(message);
            log.info(" Email identifiants envoyé avec succès à: {}", email);

        } catch (Exception e) {
            log.error(" Erreur envoi email identifiants à {}: {}", email, e.getMessage(), e);
            // ✅ RuntimeException simple et claire
            throw new RuntimeException("Erreur lors de l'envoi de l'email à: " + email, e);
        }
    }

    /**
     *  Envoyer nouveau mot de passe en cas de reset
     */
    public void envoyerNouveauMotDePasse(String email, String username, String nouveauMotDePasse) {
        log.info(" Envoi nouveau mot de passe à: {}", email);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setFrom("noreply@platformevote.com");
            message.setSubject("Réinitialisation de votre mot de passe");
            message.setText(construireMessageReset(username, nouveauMotDePasse));

            mailSender.send(message);
            log.info("Email nouveau mot de passe envoyé à: {}", email);

        } catch (Exception e) {
            log.error("Erreur envoi nouveau mot de passe à {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'envoi du nouveau mot de passe à: " + email, e);
        }
    }

    /**
     * Test rapide d'envoi d'email (pour debug)
     */
    public void testerEmail(String emailDestination) {
        log.info(" Test envoi email à: {}", emailDestination);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(emailDestination);
            message.setFrom("noreply@platformevote.com");
            message.setSubject(" Test Email - Plateforme Vote");
            message.setText("Ceci est un email de test.\n\nSi vous recevez ce message, la configuration email fonctionne !");

            mailSender.send(message);
            log.info("Email de test envoyé avec succès à: {}", emailDestination);

        } catch (Exception e) {
            log.error(" Erreur test email à {}: {}", emailDestination, e.getMessage(), e);
            throw new RuntimeException("Erreur lors du test email à: " + emailDestination, e);
        }
    }


    private String construireMessageIdentifiants(String username, String email, String motDePasse) {
        return String.format("""
            Bonjour %s,
            
            Votre compte électeur a été créé avec succès sur la Plateforme de Vote.
            
            🔑 VOS IDENTIFIANTS DE CONNEXION :
            • Email : %s
            • Mot de passe temporaire : %s
            
            ⚠️ IMPORTANT :
            • Connectez-vous dès que possible
            • Changez votre mot de passe lors de votre première connexion
            • Vous ne pouvez voter qu'une seule fois
            • Gardez vos identifiants confidentiels
            
            🌐 Lien de connexion : https://civix-five.vercel.app/login
            
            Bonne élection !
            
            ---
            L'équipe de la Plateforme de Vote
            ⚠️ Ceci est un email automatique, ne pas répondre.
            """, username, email, motDePasse);
    }

    private String construireMessageReset(String username, String nouveauMotDePasse) {
        return String.format("""
            Bonjour %s,
            
            Votre mot de passe a été réinitialisé par un administrateur.
            
            🔑 NOUVEAU MOT DE PASSE TEMPORAIRE :
            %s
            
            ⚠️ SÉCURITÉ :
            • Connectez-vous IMMÉDIATEMENT
            • Changez ce mot de passe dès votre connexion
            • Ne partagez jamais vos identifiants
            
            🌐 Lien de connexion : https://civix-five.vercel.app/login
            
            ---
            L'équipe de la Plateforme de Vote
            ⚠️ Ceci est un email automatique, ne pas répondre.
            """, username, nouveauMotDePasse);
    }
}
