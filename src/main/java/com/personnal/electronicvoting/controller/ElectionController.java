package com.personnal.electronicvoting.controller;

import com.personnal.electronicvoting.dto.ElectionDTO;
import com.personnal.electronicvoting.dto.VoteElectionDTO;
import com.personnal.electronicvoting.dto.request.CreateElectionRequest;
import com.personnal.electronicvoting.dto.request.UpdateElectionRequest;
import com.personnal.electronicvoting.dto.request.VoterElectionRequest;
import com.personnal.electronicvoting.service.ElectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

@RestController
@RequestMapping("/api/elections")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Elections", description = "APIs de gestion des élections")
public class ElectionController {

    private final ElectionService electionService;

    // ==================== GESTION ÉLECTIONS ADMINISTRATEUR ====================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Créer une élection",
            description = "Créer une nouvelle élection (accès administrateur uniquement)")
    public ResponseEntity<ElectionDTO> creerElection(
            @Valid @RequestBody CreateElectionRequest request,
            Authentication authentication) {

        log.info("🗳️ Création d'une élection par: {}", authentication.getName());

        try {
            ElectionDTO election = electionService.creerElection(request, authentication.getName());
            log.info("✅ Élection créée: {}", election.getExternalIdElection());
            return ResponseEntity.ok(election);

        } catch (RuntimeException e) {
            log.error("❌ Erreur création élection: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("💥 Erreur interne création élection: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{electionId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Modifier une élection",
            description = "Modifier une élection existante (accès administrateur uniquement)")
    public ResponseEntity<ElectionDTO> modifierElection(
            @PathVariable String electionId,
            @Valid @RequestBody UpdateElectionRequest request,
            Authentication authentication) {

        log.info("📝 Modification de l'élection {} par: {}", electionId, authentication.getName());

        try {
            ElectionDTO election = electionService.modifierElection(electionId, request, authentication.getName());
            log.info("✅ Élection modifiée: {}", electionId);
            return ResponseEntity.ok(election);

        } catch (RuntimeException e) {
            log.error("❌ Erreur modification élection: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("💥 Erreur interne modification élection: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{electionId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer une élection",
            description = "Supprimer une élection (accès administrateur uniquement)")
    public ResponseEntity<Void> supprimerElection(
            @PathVariable String electionId,
            Authentication authentication) {

        log.info("🗑️ Suppression de l'élection {} par: {}", electionId, authentication.getName());

        try {
            electionService.supprimerElection(electionId, authentication.getName());
            log.info("✅ Élection supprimée: {}", electionId);
            return ResponseEntity.noContent().build();

        } catch (RuntimeException e) {
            log.error("❌ Erreur suppression élection: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("💥 Erreur interne suppression élection: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/mes-elections")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mes élections",
            description = "Lister les élections créées par l'administrateur connecté")
    public ResponseEntity<List<ElectionDTO>> listerMesElections(Authentication authentication) {

        log.info("📋 Consultation des élections de l'administrateur: {}", authentication.getName());

        try {
            List<ElectionDTO> elections = electionService.listerElectionsAdministrateur(authentication.getName());
            log.info("📊 {} élections trouvées", elections.size());
            return ResponseEntity.ok(elections);

        } catch (Exception e) {
            log.error("💥 Erreur consultation élections administrateur: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== CONSULTATION PUBLIQUE ====================

    @GetMapping
    @Operation(summary = "Lister toutes les élections",
            description = "Obtenir la liste de toutes les élections")
    public ResponseEntity<List<ElectionDTO>> listerToutesElections() {

        log.info("📋 Consultation publique - Liste de toutes les élections");

        try {
            List<ElectionDTO> elections = electionService.listerToutesElections();
            log.info("📊 {} élections retournées", elections.size());
            return ResponseEntity.ok(elections);

        } catch (Exception e) {
            log.error("💥 Erreur consultation élections: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{electionId}")
    @Operation(summary = "Détails d'une élection",
            description = "Obtenir les détails d'une élection spécifique")
    public ResponseEntity<ElectionDTO> obtenirElection(@PathVariable String electionId) {

        log.info("🔍 Consultation de l'élection: {}", electionId);

        try {
            ElectionDTO election = electionService.obtenirElection(electionId);
            return ResponseEntity.ok(election);

        } catch (RuntimeException e) {
            log.warn("❌ Élection non trouvée: {}", electionId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("💥 Erreur consultation élection: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== ÉLECTIONS POUR ÉLECTEURS ====================

    @GetMapping("/disponibles")
    @PreAuthorize("hasRole('ELECTEUR')")
    @Operation(summary = "Élections disponibles",
            description = "Lister les élections disponibles pour l'électeur connecté")
    public ResponseEntity<List<ElectionDTO>> listerElectionsDisponibles(Authentication authentication) {

        log.info("🗳️ Consultation des élections disponibles pour: {}", authentication.getName());

        try {
            List<ElectionDTO> elections = electionService.listerElectionsDisponiblesPourElecteur(authentication.getName());
            log.info("📊 {} élections disponibles", elections.size());
            return ResponseEntity.ok(elections);

        } catch (Exception e) {
            log.error("💥 Erreur consultation élections électeur: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== VOTE ====================

    @PostMapping("/{electionId}/voter")
    @PreAuthorize("hasRole('ELECTEUR')")
    @Operation(summary = "Voter pour une élection",
            description = "Enregistrer un vote pour une élection")
    public ResponseEntity<VoteElectionDTO> voterPourElection(
            @PathVariable String electionId,
            @Valid @RequestBody VoterElectionRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        log.info("🗳️ Vote pour l'élection {} par: {}", electionId, authentication.getName());

        try {
            request.setElectionId(electionId);
            request.setAdresseIp(getClientIpAddress(httpRequest));
            request.setUserAgent(httpRequest.getHeader("User-Agent"));

            VoteElectionDTO vote = electionService.voterPourElection(request, authentication.getName());
            log.info("✅ Vote enregistré: {}", vote.getId());
            return ResponseEntity.ok(vote);

        } catch (RuntimeException e) {
            log.error("❌ Erreur vote: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("💥 Erreur interne vote: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== RÉSULTATS ====================

    @GetMapping("/{electionId}/resultats")
    @Operation(summary = "Résultats d'une élection",
            description = "Obtenir les résultats d'une élection")
    public ResponseEntity<ElectionService.ResultatsElectionDTO> obtenirResultatsElection(
            @PathVariable String electionId) {

        log.info("📊 Consultation des résultats de l'élection: {}", electionId);

        try {
            ElectionService.ResultatsElectionDTO resultats = electionService.obtenirResultatsElection(electionId);
            return ResponseEntity.ok(resultats);

        } catch (RuntimeException e) {
            log.warn("❌ Résultats non disponibles pour l'élection: {} - {}", electionId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("💥 Erreur consultation résultats: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== UTILITAIRES ====================

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        } else {
            return xForwardedForHeader.split(",")[0];
        }
    }

    // ==================== HEALTH CHECK ====================

    @GetMapping("/health")
    @Operation(summary = "Health check",
            description = "Vérifier la santé du service élections")
    public ResponseEntity<HealthCheckDTO> healthCheck() {

        log.info("🏥 Health check service élections");

        try {
            List<ElectionDTO> elections = electionService.listerToutesElections();

            HealthCheckDTO health = HealthCheckDTO.builder()
                    .status("UP")
                    .nombreElections((long) elections.size())
                    .electionsActives(elections.stream()
                            .filter(ElectionDTO::getEstActive)
                            .count())
                    .timestamp(java.time.LocalDateTime.now())
                    .message("Service élections opérationnel")
                    .build();

            return ResponseEntity.ok(health);

        } catch (Exception e) {
            log.error("💥 Health check échoué: {}", e.getMessage());

            HealthCheckDTO health = HealthCheckDTO.builder()
                    .status("DOWN")
                    .nombreElections(0L)
                    .electionsActives(0L)
                    .timestamp(java.time.LocalDateTime.now())
                    .message("Erreur service élections: " + e.getMessage())
                    .build();

            return ResponseEntity.status(503).body(health);
        }
    }

    // ==================== DTO HEALTH CHECK ====================

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class HealthCheckDTO {
        private String status;
        private Long nombreElections;
        private Long electionsActives;
        private java.time.LocalDateTime timestamp;
        private String message;
    }
}