package com.personnal.electronicvoting.controller;

import com.personnal.electronicvoting.dto.request.LoginRequest;
import com.personnal.electronicvoting.dto.request.ChangePasswordRequest;
import com.personnal.electronicvoting.dto.response.AuthResponse;
import com.personnal.electronicvoting.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentification", description = "APIs d'authentification et gestion des sessions")
public class AuthController {

    private final AuthService authService;

    // ==================== CONNEXION ====================

    /**
     *  Connexion électeur
     */
    @PostMapping("/electeur/login")
    @Operation(summary = "Connexion électeur",
            description = "Authentification d'un électeur avec email et mot de passe")
    public ResponseEntity<AuthResponse> loginElecteur(@Valid @RequestBody LoginRequest request) {
        log.info(" Tentative connexion électeur - Email: {}", request.getEmail());

        try {
            AuthResponse response = authService.authentifierElecteur(request);

            if (response == null) {
                return ResponseEntity.badRequest()
                        .body(AuthResponse.builder()
                                .token(null)
                                .build());
            }

            log.info(" Connexion électeur réussie - ID: {}", response.getUserId());
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.warn(" Échec connexion électeur: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(AuthResponse.builder()
                            .token(null)
                            .build());
        }
    }

    /**
     *  Connexion administrateur
     */
    @PostMapping("/admin/login")
    @Operation(summary = "Connexion administrateur",
            description = "Authentification d'un administrateur")
    public ResponseEntity<AuthResponse> loginAdministrateur(@Valid @RequestBody LoginRequest request) {
        log.info(" Tentative connexion admin - Email: {}", request.getEmail());

        try {
            AuthResponse response = authService.authentifierAdministrateur(request);
            log.info(" Connexion admin réussie - ID: {}", response.getUserId());
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.warn(" Échec connexion admin: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(AuthResponse.builder()
                            .token(null)
                            .build());
        }
    }

    // ==================== CHANGEMENT MOT DE PASSE ====================

    /**
     *  Changer mot de passe électeur (première connexion)
     */
    @PostMapping("/electeur/change-password")
    @Operation(summary = "Changer mot de passe électeur",
            description = "Changement de mot de passe lors de la première connexion")
    public ResponseEntity<AuthResponse> changerMotDePasseElecteur(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody ChangePasswordRequest request) {

        log.info(" Changement mot de passe électeur");

        try {
            // Extraire le token (enlever "Bearer " si présent)
            String cleanToken = token.startsWith("Bearer ") ? token.substring(7) : token;

            AuthResponse response = authService.changerMotDePasseElecteur(
                    cleanToken,
                    request.getAncienMotDePasse(),
                    request.getNouveauMotDePasse()
            );

            log.info(" Mot de passe électeur changé avec succès");
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.warn(" Échec changement mot de passe: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ==================== VALIDATION TOKEN ====================

    /**
     * ✅ Vérifier token électeur
     */
    @GetMapping("/electeur/verify")
    @Operation(summary = "Vérifier token électeur",
            description = "Valider un token électeur")
    public ResponseEntity<Boolean> verifierTokenElecteur(
            @RequestHeader("Authorization") String token) {

        try {
            String cleanToken = token.startsWith("Bearer ") ? token.substring(7) : token;
            boolean valide = authService.verifierTokenElecteur(cleanToken);
            return ResponseEntity.ok(valide);

        } catch (Exception e) {
            return ResponseEntity.ok(false);
        }
    }

    /**
     *  Vérifier token admin
     */
    @GetMapping("/admin/verify")
    @Operation(summary = "Vérifier token admin",
            description = "Valider un token administrateur")
    public ResponseEntity<Boolean> verifierTokenAdmin(
            @RequestHeader("Authorization") String token) {

        try {
            String cleanToken = token.startsWith("Bearer ") ? token.substring(7) : token;
            boolean valide = authService.verifierTokenAdmin(cleanToken);
            return ResponseEntity.ok(valide);

        } catch (Exception e) {
            return ResponseEntity.ok(false);
        }
    }

    // ==================== INFORMATION SESSION ====================

    /**
     *  Obtenir informations session électeur
     */
    @GetMapping("/electeur/session")
    @Operation(summary = "Info session électeur",
            description = "Obtenir les informations de la session électeur")
    public ResponseEntity<SessionInfoDTO> getSessionElecteur(
            @RequestHeader("Authorization") String token) {

        try {
            String cleanToken = token.startsWith("Bearer ") ? token.substring(7) : token;

            if (!authService.verifierTokenElecteur(cleanToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            var electeur = authService.obtenirElecteurDepuisToken(cleanToken);

            SessionInfoDTO sessionInfo = SessionInfoDTO.builder()
                    .userId(electeur.getExternalIdElecteur())
                    .username(electeur.getUsername())
                    .email(electeur.getEmail())
                    .role("ELECTEUR")
                    .aVote(electeur.isAVote())
                    .tokenValide(true)
                    .build();

            return ResponseEntity.ok(sessionInfo);

        } catch (Exception e) {
            log.warn(" Erreur récupération session électeur: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Obtenir informations session admin
     */
    @GetMapping("/admin/session")
    @Operation(summary = "Info session admin",
            description = "Obtenir les informations de la session admin")
    public ResponseEntity<SessionInfoDTO> getSessionAdmin(
            @RequestHeader("Authorization") String token) {

        try {
            String cleanToken = token.startsWith("Bearer ") ? token.substring(7) : token;

            if (!authService.verifierTokenAdmin(cleanToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            var admin = authService.obtenirAdminDepuisToken(cleanToken);

            SessionInfoDTO sessionInfo = SessionInfoDTO.builder()
                    .userId(admin.getExternalIdAdministrateur())
                    .username(admin.getUsername())
                    .email(admin.getEmail())
                    .role("ADMIN")
                    .aVote(null)
                    .tokenValide(true)
                    .build();

            return ResponseEntity.ok(sessionInfo);

        } catch (Exception e) {
            log.warn("❌ Erreur récupération session admin: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    // ==================== LOGOUT ====================

    /**
     * 🚪 Déconnexion (côté client principalement)
     */
    @PostMapping("/logout")
    @Operation(summary = "Déconnexion",
            description = "Déconnexion utilisateur")
    public ResponseEntity<String> logout() {
        // Pour une implémentation simple, la déconnexion est gérée côté client
        // En supprimant le token du localStorage/sessionStorage
        log.info("🚪 Demande de déconnexion");
        return ResponseEntity.ok("Déconnexion réussie");
    }

    // ==================== DTO SESSION ====================

    /**
     * 📊 DTO pour informations de session
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SessionInfoDTO {
        private String userId;
        private String username;
        private String email;
        private String role;
        private Boolean aVote;
        private boolean tokenValide;
    }
}