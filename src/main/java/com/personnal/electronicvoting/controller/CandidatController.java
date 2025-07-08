package com.personnal.electronicvoting.controller;

import com.personnal.electronicvoting.dto.CandidatDTO;
import com.personnal.electronicvoting.dto.CampagneDTO;
import com.personnal.electronicvoting.service.CandidatService;
import com.personnal.electronicvoting.service.CampagneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/candidats")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Candidats (Public)", description = "APIs publiques de consultation des candidats")
public class CandidatController {

    private final CandidatService candidatService;
    private final CampagneService campagneService;

    // ==================== CONSULTATION PUBLIQUE CANDIDATS ====================

    /**
     * 📋 Lister tous les candidats
     */
    @GetMapping
    @Operation(summary = "Liste des candidats",
            description = "Obtenir la liste de tous les candidats (accès public)")
    public ResponseEntity<List<CandidatDTO>> listerTousCandidats() {

        log.info("📋 Consultation publique - Liste des candidats");

        try {
            List<CandidatDTO> candidats = candidatService.listerTousCandidats();
            log.info("📊 {} candidats retournés", candidats.size());
            return ResponseEntity.ok(candidats);

        } catch (Exception e) {
            log.error("💥 Erreur consultation candidats: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 🔍 Obtenir un candidat par ID
     */
    @GetMapping("/{candidatId}")
    @Operation(summary = "Détails candidat",
            description = "Obtenir les détails d'un candidat spécifique")
    public ResponseEntity<CandidatService.CandidatDetailDTO> obtenirCandidat(
            @PathVariable String candidatId) {

        log.info("🔍 Consultation candidat: {}", candidatId);

        try {
            CandidatService.CandidatDetailDTO detail = candidatService.obtenirDetailCandidat(candidatId);
            return ResponseEntity.ok(detail);

        } catch (RuntimeException e) {
            log.warn("❌ Candidat non trouvé: {}", candidatId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("💥 Erreur consultation candidat: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 🔍 Rechercher candidats par nom
     */
    @GetMapping("/recherche")
    @Operation(summary = "Recherche candidats",
            description = "Rechercher des candidats par nom")
    public ResponseEntity<List<CandidatDTO>> rechercherCandidats(
            @RequestParam(required = false) String nom) {

        log.info("🔍 Recherche candidats - Terme: '{}'", nom);

        try {
            List<CandidatDTO> candidats = candidatService.rechercherCandidatsParNom(nom);
            log.info("📊 {} candidats trouvés", candidats.size());
            return ResponseEntity.ok(candidats);

        } catch (Exception e) {
            log.error("💥 Erreur recherche candidats: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== CLASSEMENTS ET STATISTIQUES ====================

    /**
     * 🏆 Classement des candidats par votes
     */
    @GetMapping("/classement")
    @Operation(summary = "Classement candidats",
            description = "Obtenir le classement des candidats par nombre de votes")
    public ResponseEntity<List<CandidatService.CandidatAvecVotesDTO>> obtenirClassement() {

        log.info("🏆 Consultation classement candidats");

        try {
            List<CandidatService.CandidatAvecVotesDTO> classement =
                    candidatService.obtenirClassementCandidats();

            log.info("📊 Classement de {} candidats", classement.size());
            return ResponseEntity.ok(classement);

        } catch (Exception e) {
            log.error("💥 Erreur classement candidats: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 🏆 Candidat en tête
     */
    @GetMapping("/en-tete")
    @Operation(summary = "Candidat en tête",
            description = "Obtenir le candidat actuellement en tête")
    public ResponseEntity<CandidatService.CandidatAvecVotesDTO> obtenirCandidatEnTete() {

        log.info("🏆 Consultation candidat en tête");

        try {
            CandidatService.CandidatAvecVotesDTO candidatEnTete =
                    candidatService.obtenirCandidatEnTete();

            return ResponseEntity.ok(candidatEnTete);

        } catch (RuntimeException e) {
            log.warn("❌ Aucun candidat trouvé");
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("💥 Erreur candidat en tête: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 📊 Statistiques détaillées des candidats
     */
    @GetMapping("/statistiques")
    @Operation(summary = "Statistiques candidats",
            description = "Obtenir les statistiques détaillées de tous les candidats")
    public ResponseEntity<List<CandidatService.StatistiquesCandidatDTO>> obtenirStatistiques() {

        log.info("📊 Consultation statistiques candidats");

        try {
            List<CandidatService.StatistiquesCandidatDTO> stats =
                    candidatService.obtenirStatistiquesDetaillees();

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("💥 Erreur statistiques candidats: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== CAMPAGNES DES CANDIDATS ====================

    /**
     * 📢 Campagnes d'un candidat
     */
    @GetMapping("/{candidatId}/campagnes")
    @Operation(summary = "Campagnes du candidat",
            description = "Obtenir toutes les campagnes d'un candidat")
    public ResponseEntity<List<CampagneDTO>> obtenirCampagnesCandidat(
            @PathVariable String candidatId) {

        log.info("📢 Consultation campagnes candidat: {}", candidatId);

        try {
            List<CampagneDTO> campagnes = candidatService.obtenirCampagnesCandidat(candidatId);
            log.info("📊 {} campagnes trouvées", campagnes.size());
            return ResponseEntity.ok(campagnes);

        } catch (RuntimeException e) {
            log.warn("❌ Candidat non trouvé: {}", candidatId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("💥 Erreur campagnes candidat: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== COMPARAISON ET ANALYSE ====================

    /**
     * ⚖️ Comparer deux candidats
     */
    @GetMapping("/comparer")
    @Operation(summary = "Comparer candidats",
            description = "Comparer deux candidats côte à côte")
    public ResponseEntity<ComparaisonCandidatsDTO> comparerCandidats(
            @RequestParam String candidat1Id,
            @RequestParam String candidat2Id) {

        log.info("⚖️ Comparaison candidats: {} vs {}", candidat1Id, candidat2Id);

        try {
            // Obtenir détails des deux candidats
            CandidatService.CandidatDetailDTO candidat1 =
                    candidatService.obtenirDetailCandidat(candidat1Id);
            CandidatService.CandidatDetailDTO candidat2 =
                    candidatService.obtenirDetailCandidat(candidat2Id);

            ComparaisonCandidatsDTO comparaison = ComparaisonCandidatsDTO.builder()
                    .candidat1(candidat1)
                    .candidat2(candidat2)
                    .differenceVotes(candidat1.getNombreVotes() - candidat2.getNombreVotes())
                    .candidatEnTete(candidat1.getNombreVotes() > candidat2.getNombreVotes() ?
                            candidat1.getCandidat().getUsername() :
                            candidat2.getCandidat().getUsername())
                    .build();

            return ResponseEntity.ok(comparaison);

        } catch (RuntimeException e) {
            log.warn("❌ Erreur comparaison: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("💥 Erreur système comparaison: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== FILTRES AVANCÉS ====================

    /**
     * 🔍 Candidats par nombre de campagnes
     */
    @GetMapping("/filtre/par-campagnes")
    @Operation(summary = "Filtrer par campagnes",
            description = "Filtrer candidats ayant un certain nombre de campagnes")
    public ResponseEntity<List<CandidatService.CandidatDetailDTO>> filtrerParNombreCampagnes(
            @RequestParam(defaultValue = "1") int minCampagnes) {

        log.info("🔍 Filtrage candidats avec min {} campagnes", minCampagnes);

        try {
            List<CandidatDTO> tousCandidats = candidatService.listerTousCandidats();

            List<CandidatService.CandidatDetailDTO> candidatsFiltres = tousCandidats.stream()
                    .map(candidat -> candidatService.obtenirDetailCandidat(candidat.getExternalIdCandidat()))
                    .filter(detail -> detail.getCampagnes().size() >= minCampagnes)
                    .toList();

            log.info("📊 {} candidats correspondent au filtre", candidatsFiltres.size());
            return ResponseEntity.ok(candidatsFiltres);

        } catch (Exception e) {
            log.error("💥 Erreur filtrage: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 📊 Candidats par tranche de votes
     */
    @GetMapping("/filtre/par-votes")
    @Operation(summary = "Filtrer par votes",
            description = "Filtrer candidats par tranche de votes")
    public ResponseEntity<List<CandidatService.CandidatAvecVotesDTO>> filtrerParVotes(
            @RequestParam(defaultValue = "0") long minVotes,
            @RequestParam(defaultValue = "999999") long maxVotes) {

        log.info("📊 Filtrage candidats avec votes entre {} et {}", minVotes, maxVotes);

        try {
            List<CandidatService.CandidatAvecVotesDTO> classement =
                    candidatService.obtenirClassementCandidats();

            List<CandidatService.CandidatAvecVotesDTO> candidatsFiltres = classement.stream()
                    .filter(candidat -> candidat.getNombreVotes() >= minVotes &&
                            candidat.getNombreVotes() <= maxVotes)
                    .toList();

            log.info("📊 {} candidats dans la tranche de votes", candidatsFiltres.size());
            return ResponseEntity.ok(candidatsFiltres);

        } catch (Exception e) {
            log.error("💥 Erreur filtrage votes: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== DTOs SPÉCIFIQUES ====================

    /**
     * ⚖️ DTO pour comparaison de candidats
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ComparaisonCandidatsDTO {
        private CandidatService.CandidatDetailDTO candidat1;
        private CandidatService.CandidatDetailDTO candidat2;
        private long differenceVotes;
        private String candidatEnTete;
    }

    // ==================== ENDPOINTS DE SANTÉ ====================

    /**
     * 🏥 Health check du service candidats
     */
    @GetMapping("/health")
    @Operation(summary = "Health check",
            description = "Vérifier la santé du service candidats")
    public ResponseEntity<HealthCheckDTO> healthCheck() {

        log.info("🏥 Health check service candidats");

        try {
            long nombreCandidats = candidatService.listerTousCandidats().size();

            HealthCheckDTO health = HealthCheckDTO.builder()
                    .status("UP")
                    .nombreCandidats(nombreCandidats)
                    .timestamp(java.time.LocalDateTime.now())
                    .message("Service candidats opérationnel")
                    .build();

            return ResponseEntity.ok(health);

        } catch (Exception e) {
            log.error("💥 Health check échoué: {}", e.getMessage());

            HealthCheckDTO health = HealthCheckDTO.builder()
                    .status("DOWN")
                    .nombreCandidats(0)
                    .timestamp(java.time.LocalDateTime.now())
                    .message("Erreur service candidats: " + e.getMessage())
                    .build();

            return ResponseEntity.status(503).body(health);
        }
    }

    /**
     * 🏥 DTO pour health check
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class HealthCheckDTO {
        private String status;
        private long nombreCandidats;
        private java.time.LocalDateTime timestamp;
        private String message;
    }
}