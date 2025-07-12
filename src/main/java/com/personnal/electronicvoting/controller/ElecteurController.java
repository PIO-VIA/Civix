package com.personnal.electronicvoting.controller;

import com.personnal.electronicvoting.dto.CandidatDTO;
import com.personnal.electronicvoting.dto.CampagneDTO;
import com.personnal.electronicvoting.dto.request.ChangePasswordRequest;
import com.personnal.electronicvoting.service.ElecteurService;
import com.personnal.electronicvoting.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/electeur")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Électeur", description = "APIs pour les électeurs")
public class ElecteurController {

    private final ElecteurService electeurService;
    private final AuthService authService;

    // ==================== MIDDLEWARE SÉCURITÉ ====================

    /**
     * 🔒 Vérifier token électeur et retourner l'électeur
     */
    private com.personnal.electronicvoting.model.Electeur verifierEtObtenirElecteur(String token) {
        String cleanToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        if (!authService.verifierTokenElecteur(cleanToken)) {
            throw new RuntimeException("Token électeur invalide");
        }
        return authService.obtenirElecteurDepuisToken(cleanToken);
    }

    // ==================== PROFIL ÉLECTEUR ====================

    /**
     * 👤 Obtenir mon profil
     */
    @GetMapping("/profil")
    @Operation(summary = "Mon profil",
            description = "Obtenir les informations de profil de l'électeur connecté")
    public ResponseEntity<ElecteurService.ElecteurProfilDTO> obtenirMonProfil(
            @RequestHeader("Authorization") String token) {

        log.info("👤 Consultation profil électeur");

        try {
            var electeur = verifierEtObtenirElecteur(token);

            ElecteurService.ElecteurProfilDTO profil =
                    electeurService.obtenirProfil(electeur.getExternalIdElecteur());

            return ResponseEntity.ok(profil);

        } catch (RuntimeException e) {
            log.warn("❌ Erreur consultation profil: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("💥 Erreur système profil: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 🔑 Changer mon mot de passe
     */
    @PutMapping("/profil/mot-de-passe")
    @Operation(summary = "Changer mot de passe",
            description = "Changer le mot de passe de l'électeur connecté")
    public ResponseEntity<String> changerMotDePasse(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody ChangePasswordRequest request) {

        log.info("Changement mot de passe électeur");

        try {
            // Nettoyer le token
            String cleanToken = token.startsWith("Bearer ") ? token.substring(7) : token;

            // Utiliser directement AuthService
            var authResponse = authService.changerMotDePasseElecteur(
                    cleanToken,
                    request.getAncienMotDePasse(),
                    request.getNouveauMotDePasse()
            );

            log.info("Mot de passe changé avec succès pour électeur: {}", authResponse.getUserId());

            return ResponseEntity.ok("Mot de passe changé avec succès");

        } catch (RuntimeException e) {
            log.warn("Erreur changement mot de passe: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        } catch (Exception e) {
            log.error("Erreur système changement mot de passe: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur système");
        }
    }

    // ==================== TABLEAU DE BORD ====================

    /**
     * 📊 Mon tableau de bord
     */
    @GetMapping("/dashboard")
    @Operation(summary = "Tableau de bord électeur",
            description = "Obtenir le tableau de bord personnalisé de l'électeur")
    public ResponseEntity<ElecteurService.TableauBordElecteurDTO> obtenirTableauBord(
            @RequestHeader("Authorization") String token) {

        log.info("📊 Consultation tableau de bord électeur");

        try {
            var electeur = verifierEtObtenirElecteur(token);

            ElecteurService.TableauBordElecteurDTO tableauBord =
                    electeurService.obtenirTableauBord(electeur.getExternalIdElecteur());

            return ResponseEntity.ok(tableauBord);

        } catch (RuntimeException e) {
            log.warn("❌ Erreur tableau de bord: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("💥 Erreur système tableau de bord: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== CONSULTATION CANDIDATS ====================

    /**
     * 🏆 Consulter tous les candidats
     */
    @GetMapping("/candidats")
    @Operation(summary = "Liste des candidats",
            description = "Consulter la liste de tous les candidats avec leurs informations")
    public ResponseEntity<List<ElecteurService.CandidatAvecStatutDTO>> consulterCandidats(
            @RequestHeader("Authorization") String token) {

        log.info("🏆 Consultation candidats par électeur");

        try {
            var electeur = verifierEtObtenirElecteur(token);

            List<ElecteurService.CandidatAvecStatutDTO> candidats =
                    electeurService.consulterCandidats(electeur.getExternalIdElecteur());

            log.info("📊 {} candidats retournés", candidats.size());
            return ResponseEntity.ok(candidats);

        } catch (RuntimeException e) {
            log.warn("❌ Erreur consultation candidats: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("💥 Erreur système consultation candidats: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 📢 Consulter campagnes d'un candidat
     */
    @GetMapping("/candidats/{candidatId}/campagnes")
    @Operation(summary = "Campagnes d'un candidat",
            description = "Consulter toutes les campagnes d'un candidat spécifique")
    public ResponseEntity<List<CampagneDTO>> consulterCampagnesCandidat(
            @RequestHeader("Authorization") String token,
            @PathVariable String candidatId) {

        log.info("📢 Consultation campagnes candidat: {}", candidatId);

        try {
            var electeur = verifierEtObtenirElecteur(token);

            List<CampagneDTO> campagnes = electeurService.consulterCampagnesCandidat(
                    electeur.getExternalIdElecteur(), candidatId);

            log.info("📊 {} campagnes trouvées pour candidat {}", campagnes.size(), candidatId);
            return ResponseEntity.ok(campagnes);

        } catch (RuntimeException e) {
            log.warn("❌ Erreur consultation campagnes: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("💥 Erreur système campagnes: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== RÉSULTATS ====================

    /**
     * 📊 Consulter résultats partiels
     */
    @GetMapping("/resultats")
    @Operation(summary = "Résultats partiels",
            description = "Consulter les résultats partiels du vote")
    public ResponseEntity<ElecteurService.ResultatsPartielsDTO> consulterResultats(
            @RequestHeader("Authorization") String token) {

        log.info("📊 Consultation résultats par électeur");

        try {
            var electeur = verifierEtObtenirElecteur(token);

            ElecteurService.ResultatsPartielsDTO resultats =
                    electeurService.consulterResultatsPartiels(electeur.getExternalIdElecteur());

            return ResponseEntity.ok(resultats);

        } catch (RuntimeException e) {
            log.warn("❌ Erreur consultation résultats: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            log.error("💥 Erreur système résultats: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== HISTORIQUE ET ACTIVITÉ ====================

    /**
     * 📜 Mon historique d'activité
     */
    @GetMapping("/historique")
    @Operation(summary = "Historique activité",
            description = "Obtenir l'historique d'activité de l'électeur")
    public ResponseEntity<HistoriqueElecteurDTO> obtenirHistorique(
            @RequestHeader("Authorization") String token) {

        log.info("📜 Consultation historique électeur");

        try {
            var electeur = verifierEtObtenirElecteur(token);

            // Pour l'instant, historique simple
            HistoriqueElecteurDTO historique = HistoriqueElecteurDTO.builder()
                    .electeurId(electeur.getExternalIdElecteur())
                    .username(electeur.getUsername())
                    .aVote(electeur.isAVote())
                    .dateInscription("Information non disponible") // À adapter selon votre modèle
                    .nombreConnexions(1) // À implémenter si nécessaire
                    .derniereActivite("Connexion actuelle")
                    .build();

            return ResponseEntity.ok(historique);

        } catch (RuntimeException e) {
            log.warn("❌ Erreur historique: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("💥 Erreur système historique: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== NOTIFICATIONS ====================

    /**
     * 📧 Mes notifications (pour futures extensions)
     */
    @GetMapping("/notifications")
    @Operation(summary = "Mes notifications",
            description = "Obtenir les notifications de l'électeur")
    public ResponseEntity<List<NotificationDTO>> obtenirNotifications(
            @RequestHeader("Authorization") String token) {

        log.info("📧 Consultation notifications électeur");

        try {
            var electeur = verifierEtObtenirElecteur(token);

            // Pour l'instant, notifications de base
            List<NotificationDTO> notifications = List.of(
                    NotificationDTO.builder()
                            .id("1")
                            .type("INFO")
                            .titre("Bienvenue !")
                            .message(electeur.isAVote() ?
                                    "Merci pour votre participation au vote." :
                                    "N'oubliez pas de voter !")
                            .dateCreation(java.time.LocalDateTime.now())
                            .lue(false)
                            .build()
            );

            return ResponseEntity.ok(notifications);

        } catch (RuntimeException e) {
            log.warn("❌ Erreur notifications: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("💥 Erreur système notifications: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== AIDE ET SUPPORT ====================

    /**
     * ❓ Obtenir aide et FAQ
     */
    @GetMapping("/aide")
    @Operation(summary = "Aide électeur",
            description = "Obtenir l'aide et FAQ pour les électeurs")
    public ResponseEntity<AideElecteurDTO> obtenirAide() {

        log.info("❓ Consultation aide électeur");

        AideElecteurDTO aide = AideElecteurDTO.builder()
                .faq(List.of(
                        FaqItemDTO.builder()
                                .question("Comment voter ?")
                                .reponse("Consultez la liste des candidats, leurs campagnes, puis cliquez sur 'Voter' pour votre candidat préféré.")
                                .build(),
                        FaqItemDTO.builder()
                                .question("Puis-je changer mon vote ?")
                                .reponse("Non, vous ne pouvez voter qu'une seule fois. Réfléchissez bien avant de valider.")
                                .build(),
                        FaqItemDTO.builder()
                                .question("Quand puis-je voir les résultats ?")
                                .reponse("Les résultats partiels sont visibles en temps réel dans l'onglet 'Résultats'.")
                                .build()
                ))
                .contactSupport("admin@platformevote.com")
                .guideUtilisation("1. Connectez-vous 2. Consultez les candidats 3. Votez 4. Consultez les résultats")
                .build();

        return ResponseEntity.ok(aide);
    }

    // ==================== DTOs SPÉCIFIQUES ====================

    /**
     * 📜 DTO pour historique électeur
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class HistoriqueElecteurDTO {
        private String electeurId;
        private String username;
        private boolean aVote;
        private String dateInscription;
        private int nombreConnexions;
        private String derniereActivite;
    }

    /**
     * 📧 DTO pour notification
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class NotificationDTO {
        private String id;
        private String type;
        private String titre;
        private String message;
        private java.time.LocalDateTime dateCreation;
        private boolean lue;
    }

    /**
     * ❓ DTO pour aide électeur
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AideElecteurDTO {
        private List<FaqItemDTO> faq;
        private String contactSupport;
        private String guideUtilisation;
    }

    /**
     * ❓ DTO pour item FAQ
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class FaqItemDTO {
        private String question;
        private String reponse;
    }
}