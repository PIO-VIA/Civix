package com.personnal.electronicvoting.controller;

import com.personnal.electronicvoting.dto.*;
import com.personnal.electronicvoting.dto.request.*;
import com.personnal.electronicvoting.service.AdministrateurService;
import com.personnal.electronicvoting.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Administration", description = "APIs de gestion administrative")
public class AdministrateurController {

    private final AdministrateurService administrateurService;
    private final AuthService authService;

    // ==================== MIDDLEWARE SÉCURITÉ ====================

    /**
     * Vérifier token admin dans les headers
     */
    private void verifierTokenAdmin(String token) {
        String cleanToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        if (!authService.verifierTokenAdmin(cleanToken)) {
            throw new RuntimeException("Token administrateur invalide");
        }
    }

    // ==================== GESTION ÉLECTEURS ====================

    /**
     *  Créer un électeur
     */
    @PostMapping("/electeurs")
    @Operation(summary = "Créer électeur",
            description = "Créer un nouvel électeur avec envoi automatique des identifiants par email")
    public ResponseEntity<ElecteurDTO> creerElecteur(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody CreateElecteurAdminRequest request) {

        log.info(" Admin - Création électeur: {}", request.getUsername());

        try {
            verifierTokenAdmin(token);

            ElecteurDTO electeur = administrateurService.creerElecteur(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(electeur);

        } catch (RuntimeException e) {
            log.warn(" Erreur création électeur: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     *  Lister tous les électeurs avec pagination
     */
    @GetMapping("/electeurs")
    @Operation(summary = "Lister électeurs",
            description = "Obtenir la liste paginée des électeurs")
    public ResponseEntity<List<ElecteurDTO>> listerElecteurs(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info(" Admin - Liste électeurs (page: {}, size: {})", page, size);

        try {
            verifierTokenAdmin(token);

            List<ElecteurDTO> electeurs = administrateurService.listerElecteurs();
            return ResponseEntity.ok(electeurs);

        } catch (RuntimeException e) {
            log.warn(" Erreur liste électeurs: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     *  Obtenir un électeur par ID
     */
    @GetMapping("/electeurs/{electeurId}")
    @Operation(summary = "Obtenir électeur",
            description = "Obtenir les détails d'un électeur")
    public ResponseEntity<ElecteurDTO> obtenirElecteur(
            @RequestHeader("Authorization") String token,
            @PathVariable String electeurId) {

        log.info(" Admin - Consultation électeur: {}", electeurId);

        try {
            verifierTokenAdmin(token);

            ElecteurDTO electeur = administrateurService.trouverElecteur(electeurId);
            return ResponseEntity.ok(electeur);

        } catch (RuntimeException e) {
            log.warn(" Électeur non trouvé: {}", electeurId);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     *  Modifier un électeur
     */
    @PutMapping("/electeurs/{electeurId}")
    @Operation(summary = "Modifier électeur",
            description = "Modifier les informations d'un électeur")
    public ResponseEntity<ElecteurDTO> modifierElecteur(
            @RequestHeader("Authorization") String token,
            @PathVariable String electeurId,
            @Valid @RequestBody UpdateElecteurRequest request) {

        log.info("️ Admin - Modification électeur: {}", electeurId);

        try {
            verifierTokenAdmin(token);

            ElecteurDTO electeur = administrateurService.modifierElecteur(electeurId, request);
            return ResponseEntity.ok(electeur);

        } catch (RuntimeException e) {
            log.warn(" Erreur modification électeur: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     *  Supprimer un électeur
     */
    @DeleteMapping("/electeurs/{electeurId}")
    @Operation(summary = "Supprimer électeur",
            description = "Supprimer un électeur (impossible s'il a voté)")
    public ResponseEntity<Void> supprimerElecteur(
            @RequestHeader("Authorization") String token,
            @PathVariable String electeurId) {

        log.info(" Admin - Suppression électeur: {}", electeurId);

        try {
            verifierTokenAdmin(token);

            administrateurService.supprimerElecteur(electeurId);
            return ResponseEntity.noContent().build();

        } catch (RuntimeException e) {
            log.warn(" Erreur suppression électeur: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ==================== GESTION CANDIDATS ====================

    /**
     *  Créer un candidat
     */
    @PostMapping("/candidats")
    @Operation(summary = "Créer candidat",
            description = "Créer un nouveau candidat")
    public ResponseEntity<CandidatDTO> creerCandidat(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody CreateCandidatRequest request) {

        log.info(" Admin - Création candidat: {}", request.getUsername());

        try {
            verifierTokenAdmin(token);

            CandidatDTO candidat = administrateurService.creerCandidat(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(candidat);

        } catch (RuntimeException e) {
            log.warn(" Erreur création candidat: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     *  Lister tous les candidats
     */
    @GetMapping("/candidats")
    @Operation(summary = "Lister candidats",
            description = "Obtenir la liste des candidats")
    public ResponseEntity<List<CandidatDTO>> listerCandidats(
            @RequestHeader("Authorization") String token) {

        log.info(" Admin - Liste candidats");

        try {
            verifierTokenAdmin(token);

            List<CandidatDTO> candidats = administrateurService.listerCandidats();
            return ResponseEntity.ok(candidats);

        } catch (RuntimeException e) {
            log.warn("Erreur liste candidats: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     *  Modifier un candidat
     */
    @PutMapping("/candidats/{candidatId}")
    @Operation(summary = "Modifier candidat",
            description = "Modifier les informations d'un candidat")
    public ResponseEntity<CandidatDTO> modifierCandidat(
            @RequestHeader("Authorization") String token,
            @PathVariable String candidatId,
            @Valid @RequestBody UpdateCandidatRequest request) {

        log.info("️ Admin - Modification candidat: {}", candidatId);

        try {
            verifierTokenAdmin(token);

            CandidatDTO candidat = administrateurService.modifierCandidat(candidatId, request);
            return ResponseEntity.ok(candidat);

        } catch (RuntimeException e) {
            log.warn(" Erreur modification candidat: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     *  Supprimer un candidat
     */
    @DeleteMapping("/candidats/{candidatId}")
    @Operation(summary = "Supprimer candidat",
            description = "Supprimer un candidat (impossible s'il a des votes)")
    public ResponseEntity<Void> supprimerCandidat(
            @RequestHeader("Authorization") String token,
            @PathVariable String candidatId) {

        log.info(" Admin - Suppression candidat: {}", candidatId);

        try {
            verifierTokenAdmin(token);

            administrateurService.supprimerCandidat(candidatId);
            return ResponseEntity.noContent().build();

        } catch (RuntimeException e) {
            log.warn(" Erreur suppression candidat: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ==================== GESTION CAMPAGNES ====================

    /**
     *  Créer une campagne
     */
    @PostMapping("/campagnes")
    @Operation(summary = "Créer campagne",
            description = "Créer une nouvelle campagne pour un candidat")
    public ResponseEntity<CampagneDTO> creerCampagne(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody CreateCampagneRequest request) {

        log.info(" Admin - Création campagne pour candidat: {}", request.getCandidatId());

        try {
            verifierTokenAdmin(token);

            CampagneDTO campagne = administrateurService.creerCampagne(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(campagne);

        } catch (RuntimeException e) {
            log.warn(" Erreur création campagne: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     *  Lister toutes les campagnes
     */
    @GetMapping("/campagnes")
    @Operation(summary = "Lister campagnes",
            description = "Obtenir la liste des campagnes")
    public ResponseEntity<List<CampagneDTO>> listerCampagnes(
            @RequestHeader("Authorization") String token) {

        log.info(" Admin - Liste campagnes");

        try {
            verifierTokenAdmin(token);

            List<CampagneDTO> campagnes = administrateurService.listerCampagnes();
            return ResponseEntity.ok(campagnes);

        } catch (RuntimeException e) {
            log.warn(" Erreur liste campagnes: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * ️ Modifier une campagne
     */
    @PutMapping("/campagnes/{campagneId}")
    @Operation(summary = "Modifier campagne",
            description = "Modifier une campagne")
    public ResponseEntity<CampagneDTO> modifierCampagne(
            @RequestHeader("Authorization") String token,
            @PathVariable String campagneId,
            @Valid @RequestBody UpdateCampagneRequest request) {

        log.info("️ Admin - Modification campagne: {}", campagneId);

        try {
            verifierTokenAdmin(token);

            CampagneDTO campagne = administrateurService.modifierCampagne(campagneId, request);
            return ResponseEntity.ok(campagne);

        } catch (RuntimeException e) {
            log.warn(" Erreur modification campagne: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     *  Supprimer une campagne
     */
    @DeleteMapping("/campagnes/{campagneId}")
    @Operation(summary = "Supprimer campagne",
            description = "Supprimer une campagne")
    public ResponseEntity<Void> supprimerCampagne(
            @RequestHeader("Authorization") String token,
            @PathVariable String campagneId) {

        log.info(" Admin - Suppression campagne: {}", campagneId);

        try {
            verifierTokenAdmin(token);

            administrateurService.supprimerCampagne(campagneId);
            return ResponseEntity.noContent().build();

        } catch (RuntimeException e) {
            log.warn(" Erreur suppression campagne: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ==================== TABLEAU DE BORD ADMINISTRATEUR ====================

    /**
     *  Tableau de bord administrateur
     */
    @GetMapping("/dashboard")
    @Operation(summary = "Tableau de bord admin",
            description = "Obtenir les statistiques administratives")
    public ResponseEntity<AdministrateurService.StatistiquesAdminDTO> obtenirTableauBord(
            @RequestHeader("Authorization") String token) {

        log.info(" Admin - Consultation tableau de bord");

        try {
            verifierTokenAdmin(token);

            AdministrateurService.StatistiquesAdminDTO stats = administrateurService.obtenirStatistiques();
            return ResponseEntity.ok(stats);

        } catch (RuntimeException e) {
            log.warn(" Erreur tableau de bord: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    // ==================== ACTIONS EN MASSE ====================

    /**
     *  Renvoyer identifiants à un électeur
     */
    @PostMapping("/electeurs/{electeurId}/resend-credentials")
    @Operation(summary = "Renvoyer identifiants",
            description = "Renvoyer les identifiants à un électeur")
    public ResponseEntity<String> renvoyerIdentifiants(
            @RequestHeader("Authorization") String token,
            @PathVariable String electeurId) {

        log.info(" Admin - Renvoi identifiants électeur: {}", electeurId);

        try {
            verifierTokenAdmin(token);

            // Créer une demande de reset mot de passe
            UpdateElecteurRequest resetRequest = UpdateElecteurRequest.builder()
                    .resetMotDePasse(true)
                    .build();

            administrateurService.modifierElecteur(electeurId, resetRequest);
            return ResponseEntity.ok("Nouveaux identifiants envoyés par email");

        } catch (RuntimeException e) {
            log.warn(" Erreur renvoi identifiants: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    /**
     * Export des données (CSV simple pour l'instant)
     */
    @GetMapping("/export/electeurs")
    @Operation(summary = "Export électeurs",
            description = "Exporter la liste des électeurs")
    public ResponseEntity<String> exporterElecteurs(
            @RequestHeader("Authorization") String token) {

        log.info(" Admin - Export électeurs");

        try {
            verifierTokenAdmin(token);

            List<ElecteurDTO> electeurs = administrateurService.listerElecteurs();

            StringBuilder csv = new StringBuilder();
            csv.append("ID,Username,Email,A Vote\n");

            for (ElecteurDTO electeur : electeurs) {
                csv.append(String.format("%s,%s,%s,%s\n",
                        electeur.getExternalIdElecteur(),
                        electeur.getUsername(),
                        electeur.getEmail(),
                        electeur.isAVote() ? "Oui" : "Non"));
            }

            return ResponseEntity.ok()
                    .header("Content-Type", "text/csv")
                    .header("Content-Disposition", "attachment; filename=electeurs.csv")
                    .body(csv.toString());

        } catch (RuntimeException e) {
            log.warn("❌ Erreur export: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}