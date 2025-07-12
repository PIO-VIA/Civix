package com.personnal.electronicvoting.controller;

import com.personnal.electronicvoting.dto.request.CreateAdministrateurRequest;
import com.personnal.electronicvoting.model.Administrateur;
import com.personnal.electronicvoting.repository.AdministrateurRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/setup")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Setup", description = "Initialisation de la plateforme")
public class AdminSetupController {

    private final AdministrateurRepository administrateurRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 🔧 Créer le premier administrateur (uniquement si aucun admin n'existe)
     */
    @PostMapping("/first-admin")
    @Operation(summary = "Créer premier admin",
            description = "Créer le premier administrateur (disponible uniquement si aucun admin n'existe)")
    public ResponseEntity<String> creerPremierAdmin(@Valid @RequestBody CreateAdministrateurRequest request) {

        log.info("🔧 Tentative création premier admin: {}", request.getUsername());

        try {
            // Vérifier qu'aucun admin n'existe déjà
            long nombreAdmins = administrateurRepository.count();
            if (nombreAdmins > 0) {
                log.warn("⚠️ Tentative création admin alors qu'il en existe déjà");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("❌ Des administrateurs existent déjà. Endpoint désactivé pour sécurité.");
            }

            // Vérifier unicité email
            if (administrateurRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest()
                        .body("❌ Un administrateur avec cet email existe déjà");
            }

            // Vérifier unicité username
            if (administrateurRepository.existsByUsername(request.getUsername())) {
                return ResponseEntity.badRequest()
                        .body("❌ Un administrateur avec ce nom d'utilisateur existe déjà");
            }

            // Créer l'administrateur
            Administrateur admin = new Administrateur();
            admin.setUsername(request.getUsername());
            admin.setEmail(request.getEmail());
            admin.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
            admin.setEmpreinteDigitale(request.getEmpreinteDigitale());

            Administrateur adminSauve = administrateurRepository.save(admin);

            log.info("✅ Premier administrateur créé avec succès - ID: {}", adminSauve.getExternalIdAdministrateur());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(String.format("✅ Premier administrateur créé avec succès !\n" +
                                    "📧 Email: %s\n" +
                                    "👤 Username: %s\n" +
                                    "🆔 ID: %s\n\n" +
                                    "Vous pouvez maintenant vous connecter via POST /api/auth/admin/login",
                            adminSauve.getEmail(),
                            adminSauve.getUsername(),
                            adminSauve.getExternalIdAdministrateur()));

        } catch (Exception e) {
            log.error(" Erreur création premier admin: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Erreur lors de la création de l'administrateur");
        }
    }

    /**
     * ️ Vérifier le statut du setup
     */
    @GetMapping("/status")
    @Operation(summary = "Statut setup",
            description = "Vérifier si la plateforme est initialisée")
    public ResponseEntity<SetupStatusDTO> obtenirStatutSetup() {

        log.info(" Vérification statut setup");

        long nombreAdmins = administrateurRepository.count();

        SetupStatusDTO status = SetupStatusDTO.builder()
                .plateformeInitialisee(nombreAdmins > 0)
                .nombreAdministrateurs(nombreAdmins)
                .peutCreerPremierAdmin(nombreAdmins == 0)
                .message(nombreAdmins > 0 ?
                        " Plateforme initialisée avec " + nombreAdmins + " administrateur(s)" :
                        " Aucun administrateur. Utilisez POST /api/setup/first-admin pour initialiser")
                .build();

        return ResponseEntity.ok(status);
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SetupStatusDTO {
        private boolean plateformeInitialisee;
        private long nombreAdministrateurs;
        private boolean peutCreerPremierAdmin;
        private String message;
    }
}