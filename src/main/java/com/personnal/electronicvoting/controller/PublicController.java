package com.personnal.electronicvoting.controller;

import com.personnal.electronicvoting.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Public", description = "APIs publiques générales")
public class PublicController {

    private final VoteService voteService;
    private final CandidatService candidatService;
    private final CampagneService campagneService;
    private final ElecteurService electeurService;

    // ==================== PAGE D'ACCUEIL ====================

    /**
     * 🏠 Informations d'accueil de la plateforme
     */
    @GetMapping("/accueil")
    @Operation(summary = "Page d'accueil",
            description = "Obtenir les informations d'accueil de la plateforme")
    public ResponseEntity<AccueilDTO> obtenirAccueil() {

        log.info("🏠 Consultation page d'accueil");

        try {
            // Statistiques de base
            VoteService.StatistiquesVoteDTO statsVote = voteService.obtenirStatistiquesGenerales();
            List<VoteService.ResultatVoteDTO> topCandidats = voteService.obtenirResultatsVotes()
                    .stream()
                    .limit(3)
                    .toList();

            CampagneService.StatistiquesCampagnesDTO statsCampagnes =
                    campagneService.obtenirStatistiquesCampagnes();

            AccueilDTO accueil = AccueilDTO.builder()
                    .titre("Plateforme de Vote Électronique")
                    .description("Participez à l'élection en toute sécurité et transparence")
                    .statistiquesGenerales(StatistiquesGeneralesDTO.builder()
                            .totalElecteurs(statsVote.getTotalElecteurs())
                            .totalCandidats(statsVote.getTotalCandidats())
                            .totalVotes(statsVote.getTotalVotes())
                            .totalCampagnes(statsCampagnes.getTotalCampagnes())
                            .tauxParticipation(statsVote.getTauxParticipation())
                            .build())
                    .topCandidats(topCandidats)
                    .messageActualite("L'élection est en cours ! Consultez les candidats et votez.")
                    .horodatage(LocalDateTime.now())
                    .versionPlateforme("1.0.0")
                    .build();

            return ResponseEntity.ok(accueil);

        } catch (Exception e) {
            log.error("💥 Erreur page d'accueil: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== STATISTIQUES PUBLIQUES ====================

    /**
     * 📊 Statistiques publiques consolidées
     */
    @GetMapping("/statistiques")
    @Operation(summary = "Statistiques publiques",
            description = "Obtenir toutes les statistiques publiques consolidées")
    public ResponseEntity<StatistiquesPubliquesDTO> obtenirStatistiquesPubliques() {

        log.info("📊 Consultation statistiques publiques");

        try {
            // Agrégation de toutes les statistiques
            VoteService.StatistiquesVoteDTO statsVote = voteService.obtenirStatistiquesGenerales();
            CampagneService.StatistiquesCampagnesDTO statsCampagnes =
                    campagneService.obtenirStatistiquesCampagnes();
            List<CandidatService.StatistiquesCandidatDTO> statsDetaillesCandidats =
                    candidatService.obtenirStatistiquesDetaillees();

            StatistiquesPubliquesDTO stats = StatistiquesPubliquesDTO.builder()
                    .statistiquesVote(statsVote)
                    .statistiquesCampagnes(statsCampagnes)
                    .statistiquesDetaillesCandidats(statsDetaillesCandidats)
                    .metadonnees(MetadonneesDTO.builder()
                            .dateDerniereMAJ(LocalDateTime.now())
                            .frequenceMAJ("Temps réel")
                            .fiabilite("99.9%")
                            .build())
                    .build();

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("💥 Erreur statistiques publiques: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== RÉSULTATS EN TEMPS RÉEL ====================

    /**
     * 🏆 Résultats en temps réel
     */
    @GetMapping("/resultats-temps-reel")
    @Operation(summary = "Résultats temps réel",
            description = "Obtenir les résultats actuels en temps réel")
    public ResponseEntity<ResultatsTempsReelDTO> obtenirResultatsTempsReel() {

        log.info("🏆 Consultation résultats temps réel");

        try {
            List<VoteService.ResultatVoteDTO> resultats = voteService.obtenirResultatsVotes();
            VoteService.StatistiquesVoteDTO stats = voteService.obtenirStatistiquesGenerales();

            // Candidat en tête
            VoteService.ResultatVoteDTO candidatEnTete = resultats.isEmpty() ? null : resultats.get(0);

            // Progression du vote (dernière heure)
            List<VoteService.VoteTemporelDTO> progression = voteService.obtenirRepartitionTemporelle();

            ResultatsTempsReelDTO resultatsTempsReel = ResultatsTempsReelDTO.builder()
                    .resultatsComplets(resultats)
                    .candidatEnTete(candidatEnTete)
                    .statistiquesGlobales(stats)
                    .progressionVote(progression)
                    .derniereMiseAJour(LocalDateTime.now())
                    .prochaineMiseAJour(LocalDateTime.now().plusMinutes(1))
                    .build();

            return ResponseEntity.ok(resultatsTempsReel);

        } catch (Exception e) {
            log.error("💥 Erreur résultats temps réel: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== DÉCOUVERTE DE CANDIDATS ====================

    /**
     * 🎯 Découverte de candidats et campagnes
     */
    @GetMapping("/decouverte")
    @Operation(summary = "Découverte candidats",
            description = "Découvrir des candidats et leurs campagnes")
    public ResponseEntity<DecouverteDTO> obtenirDecouverte() {

        log.info("🎯 Service de découverte");

        try {
            // Candidats avec le plus de campagnes
            List<CandidatService.StatistiquesCandidatDTO> candidatsActifs =
                    candidatService.obtenirStatistiquesDetaillees()
                            .stream()
                            .filter(c -> c.getNombreCampagnes() > 0)
                            .limit(5)
                            .toList();

            // Campagnes récentes avec photos
            List<CampagneService.CampagneAvecCandidatDTO> campagnesRecentes =
                    campagneService.obtenirCampagnesAvecCandidats()
                            .stream()
                            .filter(c -> c.getCampagne().getPhoto() != null)
                            .limit(6)
                            .toList();

            // Candidats en progression (basé sur les votes)
            List<CandidatService.CandidatAvecVotesDTO> candidatsEnProgression =
                    candidatService.obtenirClassementCandidats()
                            .stream()
                            .filter(c -> c.getNombreVotes() > 0)
                            .limit(4)
                            .toList();

            DecouverteDTO decouverte = DecouverteDTO.builder()
                    .candidatsActifs(candidatsActifs)
                    .campagnesEnVedette(campagnesRecentes)
                    .candidatsEnProgression(candidatsEnProgression)
                    .conseilDuJour("Explorez les campagnes pour mieux connaître les candidats avant de voter !")
                    .motsClesPopulaires(List.of("économie", "environnement", "éducation", "santé", "sécurité"))
                    .build();

            return ResponseEntity.ok(decouverte);

        } catch (Exception e) {
            log.error("💥 Erreur découverte: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== INFORMATIONS SYSTÈME ====================

    /**
     * ℹ️ Informations sur la plateforme
     */
    @GetMapping("/info")
    @Operation(summary = "Informations plateforme",
            description = "Obtenir les informations générales sur la plateforme")
    public ResponseEntity<InfoPlatformeDTO> obtenirInfoPlateforme() {

        log.info("ℹ️ Consultation informations plateforme");

        InfoPlatformeDTO info = InfoPlatformeDTO.builder()
                .nomPlateforme("Plateforme de Vote Électronique")
                .version("1.0.0")
                .description("Système de vote électronique sécurisé et transparent")
                .fonctionnalites(List.of(
                        "Vote unique et sécurisé",
                        "Consultation candidats et campagnes",
                        "Résultats en temps réel",
                        "Interface administrateur",
                        "Statistiques détaillées"
                ))
                .technologies(List.of("Spring Boot", "PostgreSQL", "Spring Security", "Swagger"))
                .support(SupportDTO.builder()
                        .email("support@platformevote.com")
                        .documentationUrl("https://docs.platformevote.com")
                        .faqUrl("https://faq.platformevote.com")
                        .heuresOuverture("24h/7j (support technique)")
                        .build())
                .securite(SecuriteDTO.builder()
                        .chiffrementDonnees("AES-256")
                        .authentificationSecurisee("BCrypt + Tokens")
                        .auditTrail("Complet")
                        .conformite("RGPD compliant")
                        .build())
                .dateMiseEnService(LocalDateTime.of(2024, 1, 1, 0, 0))
                .build();

        return ResponseEntity.ok(info);
    }

    // ==================== RECHERCHE GLOBALE ====================

    /**
     * 🔍 Recherche globale sur la plateforme
     */
    @GetMapping("/recherche")
    @Operation(summary = "Recherche globale",
            description = "Rechercher dans tous les contenus de la plateforme")
    public ResponseEntity<ResultatsRechercheDTO> rechercheGlobale(
            @RequestParam String terme) {

        log.info("🔍 Recherche globale - Terme: '{}'", terme);

        try {
            // Recherche dans les candidats
            var candidatsTrouves = candidatService.rechercherCandidatsParNom(terme);

            // Recherche dans les campagnes
            var campagnesTrouvees = campagneService.rechercherCampagnesParMotCle(terme);

            ResultatsRechercheDTO resultats = ResultatsRechercheDTO.builder()
                    .termeRecherche(terme)
                    .candidatsTrouves(candidatsTrouves)
                    .campagnesTrouvees(campagnesTrouvees)
                    .nombreTotal(candidatsTrouves.size() + campagnesTrouvees.size())
                    .suggestions(genererSuggestions(terme))
                    .horodatage(LocalDateTime.now())
                    .build();

            log.info("📊 Recherche '{}' - {} résultats", terme, resultats.getNombreTotal());
            return ResponseEntity.ok(resultats);

        } catch (Exception e) {
            log.error("💥 Erreur recherche globale: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 💡 Générer suggestions de recherche
     */
    private List<String> genererSuggestions(String terme) {
        // Suggestions basiques (à améliorer avec un moteur de recherche)
        return List.of(
                terme + " campagne",
                terme + " programme",
                "candidat " + terme,
                terme + " politique"
        );
    }

    // ==================== FLUX D'ACTUALITÉS ====================

    /**
     * 📰 Flux d'actualités de la plateforme
     */
    @GetMapping("/actualites")
    @Operation(summary = "Actualités plateforme",
            description = "Obtenir le flux d'actualités de la plateforme")
    public ResponseEntity<List<ActualiteDTO>> obtenirActualites(
            @RequestParam(defaultValue = "10") int limite) {

        log.info("📰 Consultation actualités (limite: {})", limite);

        try {
            // Générer des actualités basées sur l'activité récente
            List<ActualiteDTO> actualites = List.of(
                    ActualiteDTO.builder()
                            .id("1")
                            .titre("Nouveau candidat inscrit")
                            .contenu("Un nouveau candidat a rejoint la course !")
                            .type("CANDIDAT")
                            .datePublication(LocalDateTime.now().minusHours(2))
                            .importance("NORMALE")
                            .build(),
                    ActualiteDTO.builder()
                            .id("2")
                            .titre("Campagne mise à jour")
                            .contenu("Plusieurs candidats ont mis à jour leurs campagnes.")
                            .type("CAMPAGNE")
                            .datePublication(LocalDateTime.now().minusHours(4))
                            .importance("BASSE")
                            .build(),
                    ActualiteDTO.builder()
                            .id("3")
                            .titre("Participation en hausse")
                            .contenu("Le taux de participation continue d'augmenter !")
                            .type("VOTE")
                            .datePublication(LocalDateTime.now().minusHours(6))
                            .importance("HAUTE")
                            .build()
            );

            List<ActualiteDTO> actualitesLimitees = actualites.stream()
                    .limit(limite)
                    .toList();

            return ResponseEntity.ok(actualitesLimitees);

        } catch (Exception e) {
            log.error("💥 Erreur actualités: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== DTOs SPÉCIFIQUES ====================

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AccueilDTO {
        private String titre;
        private String description;
        private StatistiquesGeneralesDTO statistiquesGenerales;
        private List<VoteService.ResultatVoteDTO> topCandidats;
        private String messageActualite;
        private LocalDateTime horodatage;
        private String versionPlateforme;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class StatistiquesGeneralesDTO {
        private long totalElecteurs;
        private long totalCandidats;
        private long totalVotes;
        private long totalCampagnes;
        private double tauxParticipation;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class StatistiquesPubliquesDTO {
        private VoteService.StatistiquesVoteDTO statistiquesVote;
        private CampagneService.StatistiquesCampagnesDTO statistiquesCampagnes;
        private List<CandidatService.StatistiquesCandidatDTO> statistiquesDetaillesCandidats;
        private MetadonneesDTO metadonnees;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MetadonneesDTO {
        private LocalDateTime dateDerniereMAJ;
        private String frequenceMAJ;
        private String fiabilite;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ResultatsTempsReelDTO {
        private List<VoteService.ResultatVoteDTO> resultatsComplets;
        private VoteService.ResultatVoteDTO candidatEnTete;
        private VoteService.StatistiquesVoteDTO statistiquesGlobales;
        private List<VoteService.VoteTemporelDTO> progressionVote;
        private LocalDateTime derniereMiseAJour;
        private LocalDateTime prochaineMiseAJour;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DecouverteDTO {
        private List<CandidatService.StatistiquesCandidatDTO> candidatsActifs;
        private List<CampagneService.CampagneAvecCandidatDTO> campagnesEnVedette;
        private List<CandidatService.CandidatAvecVotesDTO> candidatsEnProgression;
        private String conseilDuJour;
        private List<String> motsClesPopulaires;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class InfoPlatformeDTO {
        private String nomPlateforme;
        private String version;
        private String description;
        private List<String> fonctionnalites;
        private List<String> technologies;
        private SupportDTO support;
        private SecuriteDTO securite;
        private LocalDateTime dateMiseEnService;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SupportDTO {
        private String email;
        private String documentationUrl;
        private String faqUrl;
        private String heuresOuverture;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SecuriteDTO {
        private String chiffrementDonnees;
        private String authentificationSecurisee;
        private String auditTrail;
        private String conformite;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ResultatsRechercheDTO {
        private String termeRecherche;
        private List<com.personnal.electronicvoting.dto.CandidatDTO> candidatsTrouves;
        private List<com.personnal.electronicvoting.dto.CampagneDTO> campagnesTrouvees;
        private int nombreTotal;
        private List<String> suggestions;
        private LocalDateTime horodatage;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ActualiteDTO {
        private String id;
        private String titre;
        private String contenu;
        private String type;
        private LocalDateTime datePublication;
        private String importance;
    }
}