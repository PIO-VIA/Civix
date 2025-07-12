package com.personnal.electronicvoting.controller;

import com.personnal.electronicvoting.dto.CampagneDTO;
import com.personnal.electronicvoting.service.CampagneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public/campagnes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Campagnes (Public)", description = "APIs publiques de consultation des campagnes")
public class CampagneController {

    private final CampagneService campagneService;

    // ==================== CONSULTATION PUBLIQUE CAMPAGNES ====================

    /**
     * 📋 Lister toutes les campagnes
     */
    @GetMapping
    @Operation(summary = "Liste des campagnes",
            description = "Obtenir la liste de toutes les campagnes (accès public)")
    public ResponseEntity<List<CampagneDTO>> listerToutesCampagnes() {

        log.info("📋 Consultation publique - Liste des campagnes");

        try {
            List<CampagneDTO> campagnes = campagneService.listerToutesCampagnes();
            log.info("📊 {} campagnes retournées", campagnes.size());
            return ResponseEntity.ok(campagnes);

        } catch (Exception e) {
            log.error("💥 Erreur consultation campagnes: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 🔍 Obtenir une campagne par ID
     */
    @GetMapping("/{campagneId}")
    @Operation(summary = "Détails campagne",
            description = "Obtenir les détails d'une campagne spécifique")
    public ResponseEntity<CampagneService.CampagneDetailDTO> obtenirCampagne(
            @PathVariable String campagneId) {

        log.info("🔍 Consultation campagne: {}", campagneId);

        try {
            CampagneService.CampagneDetailDTO detail = campagneService.obtenirDetailCampagne(campagneId);
            return ResponseEntity.ok(detail);

        } catch (RuntimeException e) {
            log.warn("❌ Campagne non trouvée: {}", campagneId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("💥 Erreur consultation campagne: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== RECHERCHE ET FILTRES ====================

    /**
     * 🔍 Rechercher campagnes par mot-clé
     */
    @GetMapping("/recherche")
    @Operation(summary = "Recherche campagnes",
            description = "Rechercher des campagnes par mot-clé dans la description ou nom candidat")
    public ResponseEntity<List<CampagneDTO>> rechercherCampagnes(
            @RequestParam(required = false) String motCle) {

        log.info("🔍 Recherche campagnes - Mot-clé: '{}'", motCle);

        try {
            List<CampagneDTO> campagnes = campagneService.rechercherCampagnesParMotCle(motCle);
            log.info("📊 {} campagnes trouvées", campagnes.size());
            return ResponseEntity.ok(campagnes);

        } catch (Exception e) {
            log.error("💥 Erreur recherche campagnes: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 📸 Campagnes avec photos uniquement
     */
    @GetMapping("/avec-photos")
    @Operation(summary = "Campagnes avec photos",
            description = "Obtenir uniquement les campagnes qui ont des photos")
    public ResponseEntity<List<CampagneDTO>> obtenirCampagnesAvecPhotos() {

        log.info("📸 Consultation campagnes avec photos");

        try {
            List<CampagneDTO> campagnes = campagneService.obtenirCampagnesAvecPhotos();
            log.info("📊 {} campagnes avec photos", campagnes.size());
            return ResponseEntity.ok(campagnes);

        } catch (Exception e) {
            log.error("💥 Erreur campagnes avec photos: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== CAMPAGNES ENRICHIES ====================

    /**
     * 👥 Campagnes avec informations candidat
     */
    @GetMapping("/avec-candidats")
    @Operation(summary = "Campagnes avec candidats",
            description = "Obtenir toutes les campagnes avec les informations des candidats")
    public ResponseEntity<List<CampagneService.CampagneAvecCandidatDTO>> obtenirCampagnesAvecCandidats() {

        log.info("👥 Consultation campagnes avec candidats");

        try {
            List<CampagneService.CampagneAvecCandidatDTO> campagnes =
                    campagneService.obtenirCampagnesAvecCandidats();

            log.info("📊 {} campagnes avec candidats", campagnes.size());
            return ResponseEntity.ok(campagnes);

        } catch (Exception e) {
            log.error("💥 Erreur campagnes avec candidats: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== REGROUPEMENTS ET ANALYSES ====================

    /**
     * 📊 Campagnes groupées par candidat
     */
    @GetMapping("/par-candidat")
    @Operation(summary = "Campagnes par candidat",
            description = "Obtenir les campagnes regroupées par candidat")
    public ResponseEntity<Map<String, List<CampagneDTO>>> obtenirCampagnesParCandidat() {

        log.info("📊 Regroupement campagnes par candidat");

        try {
            Map<String, List<CampagneDTO>> regroupement =
                    campagneService.obtenirCampagnesGroupeesParCandidat();

            log.info("📊 Campagnes regroupées pour {} candidats", regroupement.size());
            return ResponseEntity.ok(regroupement);

        } catch (Exception e) {
            log.error("💥 Erreur regroupement: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 📊 Répartition des campagnes par candidat
     */
    @GetMapping("/repartition")
    @Operation(summary = "Répartition campagnes",
            description = "Obtenir la répartition détaillée des campagnes par candidat")
    public ResponseEntity<List<CampagneService.RepartitionCampagnesDTO>> obtenirRepartition() {

        log.info("📊 Consultation répartition campagnes");

        try {
            List<CampagneService.RepartitionCampagnesDTO> repartition =
                    campagneService.obtenirRepartitionParCandidat();

            return ResponseEntity.ok(repartition);

        } catch (Exception e) {
            log.error("💥 Erreur répartition: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== STATISTIQUES ====================

    /**
     * 📊 Statistiques des campagnes
     */
    @GetMapping("/statistiques")
    @Operation(summary = "Statistiques campagnes",
            description = "Obtenir les statistiques globales des campagnes")
    public ResponseEntity<CampagneService.StatistiquesCampagnesDTO> obtenirStatistiques() {

        log.info("📊 Consultation statistiques campagnes");

        try {
            CampagneService.StatistiquesCampagnesDTO stats =
                    campagneService.obtenirStatistiquesCampagnes();

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("💥 Erreur statistiques: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== FILTRES AVANCÉS ====================

    /**
     *  Campagnes par longueur de description
     */
    @GetMapping("/filtre/par-longueur")
    @Operation(summary = "Filtrer par longueur",
            description = "Filtrer campagnes par longueur de description")
    public ResponseEntity<List<CampagneDTO>> filtrerParLongueurDescription(
            @RequestParam(defaultValue = "0") int minLongueur,
            @RequestParam(defaultValue = "10000") int maxLongueur) {

        log.info(" Filtrage campagnes par longueur: {} - {}", minLongueur, maxLongueur);

        try {
            List<CampagneDTO> toutesCampagnes = campagneService.listerToutesCampagnes();

            List<CampagneDTO> campagnesFiltrees = toutesCampagnes.stream()
                    .filter(campagne -> {
                        int longueur = campagne.getDescription().length();
                        return longueur >= minLongueur && longueur <= maxLongueur;
                    })
                    .toList();

            log.info(" {} campagnes correspondent au filtre", campagnesFiltrees.size());
            return ResponseEntity.ok(campagnesFiltrees);

        } catch (Exception e) {
            log.error(" Erreur filtrage longueur: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== CONSULTATION PAR CANDIDAT ====================

    /**
     *  Campagnes d'un candidat spécifique
     */
    @GetMapping("/candidat/{candidatId}")
    @Operation(summary = "Campagnes d'un candidat",
            description = "Obtenir toutes les campagnes d'un candidat spécifique")
    public ResponseEntity<List<CampagneDTO>> obtenirCampagnesCandidat(
            @PathVariable String candidatId) {

        log.info(" Consultation campagnes candidat: {}", candidatId);

        try {
            List<CampagneDTO> campagnes = campagneService.obtenirCampagnesParCandidat(candidatId);
            log.info(" {} campagnes trouvées pour candidat {}", campagnes.size(), candidatId);
            return ResponseEntity.ok(campagnes);

        } catch (RuntimeException e) {
            log.warn(" Candidat non trouvé: {}", candidatId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error(" Erreur campagnes candidat: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== TRENDING ET DÉCOUVERTE ====================

    /**
     * Campagnes tendance (les plus récentes)
     */
    @GetMapping("/tendance")
    @Operation(summary = "Campagnes tendance",
            description = "Obtenir les campagnes les plus récentes ou populaires")
    public ResponseEntity<List<CampagneService.CampagneAvecCandidatDTO>> obtenirCampagnesTendance(
            @RequestParam(defaultValue = "5") int limite) {

        log.info(" Consultation campagnes tendance (limite: {})", limite);

        try {
            List<CampagneService.CampagneAvecCandidatDTO> toutesCampagnes =
                    campagneService.obtenirCampagnesAvecCandidats();

            // Pour l'instant, on prend les premières (à améliorer avec date de création)
            List<CampagneService.CampagneAvecCandidatDTO> campagnesTendance =
                    toutesCampagnes.stream()
                            .limit(limite)
                            .toList();

            log.info(" {} campagnes tendance retournées", campagnesTendance.size());
            return ResponseEntity.ok(campagnesTendance);

        } catch (Exception e) {
            log.error(" Erreur campagnes tendance: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     *  Campagne aléatoire
     */
    @GetMapping("/aleatoire")
    @Operation(summary = "Campagne aléatoire",
            description = "Obtenir une campagne choisie aléatoirement")
    public ResponseEntity<CampagneService.CampagneAvecCandidatDTO> obtenirCampagneAleatoire() {

        log.info(" Sélection campagne aléatoire");

        try {
            List<CampagneService.CampagneAvecCandidatDTO> toutesCampagnes =
                    campagneService.obtenirCampagnesAvecCandidats();

            if (toutesCampagnes.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            int indexAleatoire = (int) (Math.random() * toutesCampagnes.size());
            CampagneService.CampagneAvecCandidatDTO campagneAleatoire =
                    toutesCampagnes.get(indexAleatoire);

            log.info(" Campagne aléatoire sélectionnée: {}",
                    campagneAleatoire.getCampagne().getExternalIdCampagne());

            return ResponseEntity.ok(campagneAleatoire);

        } catch (Exception e) {
            log.error(" Erreur campagne aléatoire: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== EXPORTS ET FORMATS ====================

    /**
     *  Export campagnes en format simplifié
     */
    @GetMapping("/export/simple")
    @Operation(summary = "Export simple",
            description = "Exporter les campagnes en format texte simple")
    public ResponseEntity<String> exporterCampagnesSimple() {

        log.info(" Export campagnes format simple");

        try {
            List<CampagneService.CampagneAvecCandidatDTO> campagnes =
                    campagneService.obtenirCampagnesAvecCandidats();

            StringBuilder export = new StringBuilder();
            export.append("=== CAMPAGNES ÉLECTORALES ===\n\n");

            for (CampagneService.CampagneAvecCandidatDTO campagne : campagnes) {
                export.append("Candidat: ").append(campagne.getCandidat().getUsername()).append("\n");
                export.append("Description: ").append(campagne.getCampagne().getDescription()).append("\n");
                export.append("Photo: ").append(campagne.getCampagne().getPhoto() != null ?
                        "Oui" : "Non").append("\n");
                export.append("---\n\n");
            }

            return ResponseEntity.ok()
                    .header("Content-Type", "text/plain; charset=utf-8")
                    .header("Content-Disposition", "attachment; filename=campagnes.txt")
                    .body(export.toString());

        } catch (Exception e) {
            log.error(" Erreur export: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== HEALTH CHECK ====================

    /**
     *  Health check du service campagnes
     */
    @GetMapping("/health")
    @Operation(summary = "Health check",
            description = "Vérifier la santé du service campagnes")
    public ResponseEntity<HealthCheckDTO> healthCheck() {

        log.info(" Health check service campagnes");

        try {
            CampagneService.StatistiquesCampagnesDTO stats = campagneService.obtenirStatistiquesCampagnes();

            HealthCheckDTO health = HealthCheckDTO.builder()
                    .status("UP")
                    .nombreCampagnes(stats.getTotalCampagnes())
                    .campagnesAvecPhotos(stats.getCampagnesAvecPhotos())
                    .timestamp(java.time.LocalDateTime.now())
                    .message("Service campagnes opérationnel")
                    .build();

            return ResponseEntity.ok(health);

        } catch (Exception e) {
            log.error(" Health check échoué: {}", e.getMessage());

            HealthCheckDTO health = HealthCheckDTO.builder()
                    .status("DOWN")
                    .nombreCampagnes(0)
                    .campagnesAvecPhotos(0)
                    .timestamp(java.time.LocalDateTime.now())
                    .message("Erreur service campagnes: " + e.getMessage())
                    .build();

            return ResponseEntity.status(503).body(health);
        }
    }

    // ==================== DTO HEALTH CHECK ====================

    /**
     *  DTO pour health check
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class HealthCheckDTO {
        private String status;
        private long nombreCampagnes;
        private long campagnesAvecPhotos;
        private java.time.LocalDateTime timestamp;
        private String message;
    }
}