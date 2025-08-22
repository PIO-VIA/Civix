package com.personnal.electronicvoting.controller;

import com.personnal.electronicvoting.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tableaux de bord", description = "APIs de tableaux de bord consolidés")
public class DashboardController {

    private final VoteService voteService;
    private final CandidatService candidatService;
    private final CampagneService campagneService;
    private final ElecteurService electeurService;
    private final AdministrateurService administrateurService;
    private final AuthService authService;

    // ==================== TABLEAU DE BORD ÉLECTEUR ====================

    /**
     * 📊 Tableau de bord complet électeur
     */
    @GetMapping("/electeur")
    @Operation(summary = "Dashboard électeur",
            description = "Tableau de bord complet pour l'électeur connecté")
    public ResponseEntity<DashboardElecteurDTO> obtenirDashboardElecteur(
            @RequestHeader("Authorization") String token) {

        log.info("📊 Dashboard électeur");

        try {
            String cleanToken = token.startsWith("Bearer ") ? token.substring(7) : token;
            if (!authService.verifierTokenElecteur(cleanToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            var electeur = authService.obtenirElecteurDepuisToken(cleanToken);
            String electeurId = electeur.getExternalIdElecteur();

            // Profil électeur
            ElecteurService.ElecteurProfilDTO profil =
                    electeurService.obtenirProfil(electeurId);

            // Statut de vote
            VoteService.StatutVoteElecteurDTO statutVote =
                    voteService.obtenirStatutVoteElecteur(electeurId);

            // Candidats disponibles
            List<ElecteurService.CandidatAvecStatutDTO> candidats =
                    electeurService.consulterCandidats(electeurId);

            // Résultats si électeur a voté
            ElecteurService.ResultatsPartielsDTO resultats = null;
            if (electeur.isAVote()) {
                resultats = electeurService.consulterResultatsPartiels(electeurId);
            }

            // Statistiques globales
            VoteService.StatistiquesVoteDTO statsGlobales = voteService.obtenirStatistiquesGenerales();

            // Recommandations
            List<String> recommendations = genererRecommandationsElecteur(electeur);

            DashboardElecteurDTO dashboard = DashboardElecteurDTO.builder()
                    .profil(profil)
                    .statutVote(statutVote)
                    .candidatsDisponibles(candidats)
                    .resultatsPartiels(resultats)
                    .statistiquesGlobales(statsGlobales)
                    .recommendations(recommendations)
                    .messageBienvenue(genererMessageBienvenue(electeur))
                    .derniereMiseAJour(LocalDate.now())
                    .build();

            return ResponseEntity.ok(dashboard);

        } catch (RuntimeException e) {
            log.warn("❌ Erreur dashboard électeur: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("💥 Erreur système dashboard électeur: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== TABLEAU DE BORD ADMINISTRATEUR ====================

    /**
     * 📊 Tableau de bord complet administrateur
     */
    @GetMapping("/admin")
    @Operation(summary = "Dashboard admin",
            description = "Tableau de bord complet pour l'administrateur")
    public ResponseEntity<DashboardAdminDTO> obtenirDashboardAdmin(
            @RequestHeader("Authorization") String token) {

        log.info("📊 Dashboard administrateur");

        try {
            String cleanToken = token.startsWith("Bearer ") ? token.substring(7) : token;
            if (!authService.verifierTokenAdmin(cleanToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Statistiques administratives
            AdministrateurService.StatistiquesAdminDTO statsAdmin =
                    administrateurService.obtenirStatistiques();

            // Résultats détaillés
            List<VoteService.ResultatVoteDTO> resultatsDetailles =
                    voteService.obtenirResultatsVotes();

            // Analyse temporelle
            List<VoteService.VoteTemporelDTO> analyseTemporelle =
                    voteService.obtenirRepartitionTemporelle();

            // Statistiques candidats
            List<CandidatService.StatistiquesCandidatDTO> statsCandidats =
                    candidatService.obtenirStatistiquesDetaillees();

            // Statistiques campagnes
            CampagneService.StatistiquesCampagnesDTO statsCampagnes =
                    campagneService.obtenirStatistiquesCampagnes();

            // Alertes et notifications
            List<AlerteDTO> alertes = genererAlertes(statsAdmin, resultatsDetailles);

            // Métriques de performance
            MetriquesPerformanceDTO metriques = calculerMetriques();

            // Actions récentes (simulation)
            List<ActionRecenteDTO> actionsRecentes = genererActionsRecentes();

            DashboardAdminDTO dashboard = DashboardAdminDTO.builder()
                    .statistiquesAdmin(statsAdmin)
                    .resultatsDetailles(resultatsDetailles)
                    .analyseTemporelle(analyseTemporelle)
                    .statistiquesCandidats(statsCandidats)
                    .statistiquesCampagnes(statsCampagnes)
                    .alertes(alertes)
                    .metriquesPerformance(metriques)
                    .actionsRecentes(actionsRecentes)
                    .resumeExecutif(genererResumeExecutif(statsAdmin))
                    .derniereMiseAJour(LocalDate.now())
                    .build();

            return ResponseEntity.ok(dashboard);

        } catch (RuntimeException e) {
            log.warn("❌ Erreur dashboard admin: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("💥 Erreur système dashboard admin: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== TABLEAU DE BORD PUBLIC ====================

    /**
     * 📊 Tableau de bord public (sans authentification)
     */
    @GetMapping("/public")
    @Operation(summary = "Dashboard public",
            description = "Tableau de bord public accessible à tous")
    public ResponseEntity<DashboardPublicDTO> obtenirDashboardPublic() {

        log.info("📊 Dashboard public");

        try {
            // Statistiques de base
            VoteService.StatistiquesVoteDTO statsVote = voteService.obtenirStatistiquesGenerales();

            // Top candidats
            List<VoteService.ResultatVoteDTO> topCandidats = voteService.obtenirResultatsVotes()
                    .stream()
                    .limit(5)
                    .toList();

            // Tendances des votes
            List<VoteService.VoteTemporelDTO> tendances = voteService.obtenirRepartitionTemporelle();

            // Statistiques campagnes
            CampagneService.StatistiquesCampagnesDTO statsCampagnes =
                    campagneService.obtenirStatistiquesCampagnes();

            // Campagnes en vedette
            List<CampagneService.CampagneAvecCandidatDTO> campagnesVedette =
                    campagneService.obtenirCampagnesAvecCandidats()
                            .stream()
                            .filter(c -> c.getCampagne().getPhoto() != null)
                            .limit(4)
                            .toList();

            // Progression de la participation
            ProgressionParticipationDTO progression = calculerProgressionParticipation(statsVote);

            DashboardPublicDTO dashboard = DashboardPublicDTO.builder()
                    .statistiquesGenerales(statsVote)
                    .topCandidats(topCandidats)
                    .tendancesVotes(tendances)
                    .statistiquesCampagnes(statsCampagnes)
                    .campagnesEnVedette(campagnesVedette)
                    .progressionParticipation(progression)
                    .messagePublic("Élection en cours - Résultats en temps réel")
                    .derniereMiseAJour(LocalDate.now())
                    .prochaineMiseAJour(LocalDate.now())
                    .build();

            return ResponseEntity.ok(dashboard);

        } catch (Exception e) {
            log.error("💥 Erreur dashboard public: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== WIDGETS SPÉCIALISÉS ====================

    /**
     * 📈 Widget de progression temps réel
     */
    @GetMapping("/widget/progression")
    @Operation(summary = "Widget progression",
            description = "Widget de progression du vote en temps réel")
    public ResponseEntity<WidgetProgressionDTO> obtenirWidgetProgression() {

        log.info("📈 Widget progression temps réel");

        try {
            VoteService.StatistiquesVoteDTO stats = voteService.obtenirStatistiquesGenerales();

            double pourcentageParticipation = stats.getTauxParticipation();
            String statutProgression;
            String couleur;

            if (pourcentageParticipation < 25) {
                statutProgression = "Démarrage lent";
                couleur = "rouge";
            } else if (pourcentageParticipation < 50) {
                statutProgression = "En progression";
                couleur = "orange";
            } else if (pourcentageParticipation < 75) {
                statutProgression = "Bonne participation";
                couleur = "bleu";
            } else {
                statutProgression = "Excellente participation";
                couleur = "vert";
            }

            WidgetProgressionDTO widget = WidgetProgressionDTO.builder()
                    .pourcentageParticipation(pourcentageParticipation)
                    .nombreVotes(stats.getTotalVotes())
                    .nombreElecteurs(stats.getTotalElecteurs())
                    .statutProgression(statutProgression)
                    .couleurIndicateur(couleur)
                    .messageMotivation(genererMessageMotivation(pourcentageParticipation))
                    .horodatage(LocalDate.now())
                    .build();

            return ResponseEntity.ok(widget);

        } catch (Exception e) {
            log.error("💥 Erreur widget progression: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 🏆 Widget podium candidats
     */
    @GetMapping("/widget/podium")
    @Operation(summary = "Widget podium",
            description = "Widget podium des 3 premiers candidats")
    public ResponseEntity<WidgetPodiumDTO> obtenirWidgetPodium() {

        log.info("🏆 Widget podium candidats");

        try {
            List<VoteService.ResultatVoteDTO> resultats = voteService.obtenirResultatsVotes();

            VoteService.ResultatVoteDTO premier = resultats.size() > 0 ? resultats.get(0) : null;
            VoteService.ResultatVoteDTO deuxieme = resultats.size() > 1 ? resultats.get(1) : null;
            VoteService.ResultatVoteDTO troisieme = resultats.size() > 2 ? resultats.get(2) : null;

            WidgetPodiumDTO widget = WidgetPodiumDTO.builder()
                    .premierPlace(premier)
                    .deuxiemePlace(deuxieme)
                    .troisiemePlace(troisieme)
                    .ecartPremierDeuxieme(calculerEcart(premier, deuxieme))
                    .messageCompetition(genererMessageCompetition(premier, deuxieme))
                    .horodatage(LocalDate.now())
                    .build();

            return ResponseEntity.ok(widget);

        } catch (Exception e) {
            log.error("💥 Erreur widget podium: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    private List<String> genererRecommandationsElecteur(
            com.personnal.electronicvoting.model.Electeur electeur) {
        if (electeur.isAVote()) {
            return List.of(
                    "Merci d'avoir voté ! Consultez les résultats en temps réel.",
                    "Partagez l'importance du vote avec vos proches.",
                    "Suivez l'évolution des résultats sur le tableau de bord."
            );
        } else {
            return List.of(
                    "Explorez les campagnes des candidats avant de voter.",
                    "Votre vote compte ! Participez à la démocratie.",
                    "Consultez les programmes détaillés des candidats."
            );
        }
    }

    private String genererMessageBienvenue(
            com.personnal.electronicvoting.model.Electeur electeur) {
        if (electeur.isAVote()) {
            return String.format("Bienvenue %s ! Vous avez participé au vote. " +
                    "Consultez les résultats en temps réel.", electeur.getUsername());
        } else {
            return String.format("Bienvenue %s ! N'oubliez pas d'exercer votre droit de vote.",
                    electeur.getUsername());
        }
    }

    private List<AlerteDTO> genererAlertes(
            AdministrateurService.StatistiquesAdminDTO stats,
            List<VoteService.ResultatVoteDTO> resultats) {
        List<AlerteDTO> alertes = new java.util.ArrayList<>();

        // Alerte participation faible
        if (stats.getTauxParticipation() < 30) {
            alertes.add(AlerteDTO.builder()
                    .type("WARNING")
                    .titre("Participation faible")
                    .message("Le taux de participation est de " + stats.getTauxParticipation() + "%")
                    .niveau("MOYEN")
                    .horodatage(LocalDate.now())
                    .build());
        }

        // Alerte candidat sans campagne
        if (stats.getTotalCandidats() > stats.getTotalCampagnes()) {
            alertes.add(AlerteDTO.builder()
                    .type("INFO")
                    .titre("Candidats sans campagne")
                    .message("Certains candidats n'ont pas encore de campagne")
                    .niveau("BAS")
                    .horodatage(LocalDate.now())
                    .build());
        }

        return alertes;
    }

    private MetriquesPerformanceDTO calculerMetriques() {
        return MetriquesPerformanceDTO.builder()
                .tempsReponseAPI(45) // ms
                .utilisateursActifs(150)
                .requetesParMinute(200)
                .tauxSucces(99.5)
                .chargeSysteme("NORMALE")
                .horodatage(LocalDate.now())
                .build();
    }

    private List<ActionRecenteDTO> genererActionsRecentes() {
        return List.of(
                ActionRecenteDTO.builder()
                        .id("1")
                        .action("Création électeur")
                        .utilisateur("admin")
                        .horodatage(LocalDate.now())
                        .statut("SUCCES")
                        .build(),
                ActionRecenteDTO.builder()
                        .id("2")
                        .action("Modification campagne")
                        .utilisateur("admin")
                        .horodatage(LocalDate.now())
                        .statut("SUCCES")
                        .build()
        );
    }

    private String genererResumeExecutif(AdministrateurService.StatistiquesAdminDTO stats) {
        return String.format(
                "L'élection progresse avec %d électeurs inscrits et un taux de participation de %.1f%%. " +
                        "%d candidats sont en lice avec %d campagnes actives.",
                stats.getTotalElecteurs(),
                stats.getTauxParticipation(),
                stats.getTotalCandidats(),
                stats.getTotalCampagnes()
        );
    }

    private ProgressionParticipationDTO calculerProgressionParticipation(
            VoteService.StatistiquesVoteDTO stats) {
        return ProgressionParticipationDTO.builder()
                .tauxActuel(stats.getTauxParticipation())
                .objectif(75.0) // Objectif de participation
                .progression(stats.getTauxParticipation() / 75.0 * 100)
                .tempsRestantEstime("2 heures") // Simulation
                .tendance("CROISSANTE")
                .build();
    }

    private String genererMessageMotivation(double pourcentage) {
        if (pourcentage < 25) {
            return "Chaque vote compte ! Encouragez la participation.";
        } else if (pourcentage < 50) {
            return "La participation s'améliore ! Continuons.";
        } else if (pourcentage < 75) {
            return "Excellente mobilisation ! Objectif en vue.";
        } else {
            return "Participation remarquable ! Démocratie en action.";
        }
    }

    private double calculerEcart(VoteService.ResultatVoteDTO premier,
                                 VoteService.ResultatVoteDTO deuxieme) {
        if (premier == null || deuxieme == null) return 0;
        return premier.getNombreVotes() - deuxieme.getNombreVotes();
    }

    private String genererMessageCompetition(VoteService.ResultatVoteDTO premier,
                                             VoteService.ResultatVoteDTO deuxieme) {
        if (premier == null) return "Aucun vote pour le moment";
        if (deuxieme == null) return premier.getCandidat().getUsername() + " mène seul";

        double ecart = calculerEcart(premier, deuxieme);
        if (ecart < 5) {
            return "Course serrée entre les deux premiers !";
        } else if (ecart < 20) {
            return "Avance modérée pour le leader.";
        } else {
            return premier.getCandidat().getUsername() + " creuse l'écart.";
        }
    }

    // ==================== DTOs ====================

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DashboardElecteurDTO {
        private ElecteurService.ElecteurProfilDTO profil;
        private VoteService.StatutVoteElecteurDTO statutVote;
        private List<ElecteurService.CandidatAvecStatutDTO> candidatsDisponibles;
        private ElecteurService.ResultatsPartielsDTO resultatsPartiels;
        private VoteService.StatistiquesVoteDTO statistiquesGlobales;
        private List<String> recommendations;
        private String messageBienvenue;
        private LocalDate derniereMiseAJour;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DashboardAdminDTO {
        private AdministrateurService.StatistiquesAdminDTO statistiquesAdmin;
        private List<VoteService.ResultatVoteDTO> resultatsDetailles;
        private List<VoteService.VoteTemporelDTO> analyseTemporelle;
        private List<CandidatService.StatistiquesCandidatDTO> statistiquesCandidats;
        private CampagneService.StatistiquesCampagnesDTO statistiquesCampagnes;
        private List<AlerteDTO> alertes;
        private MetriquesPerformanceDTO metriquesPerformance;
        private List<ActionRecenteDTO> actionsRecentes;
        private String resumeExecutif;
        private LocalDate derniereMiseAJour;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DashboardPublicDTO {
        private VoteService.StatistiquesVoteDTO statistiquesGenerales;
        private List<VoteService.ResultatVoteDTO> topCandidats;
        private List<VoteService.VoteTemporelDTO> tendancesVotes;
        private CampagneService.StatistiquesCampagnesDTO statistiquesCampagnes;
        private List<CampagneService.CampagneAvecCandidatDTO> campagnesEnVedette;
        private ProgressionParticipationDTO progressionParticipation;
        private String messagePublic;
        private LocalDate derniereMiseAJour;
        private LocalDate prochaineMiseAJour;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AlerteDTO {
        private String type;
        private String titre;
        private String message;
        private String niveau;
        private LocalDate horodatage;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MetriquesPerformanceDTO {
        private int tempsReponseAPI;
        private int utilisateursActifs;
        private int requetesParMinute;
        private double tauxSucces;
        private String chargeSysteme;
        private LocalDate horodatage;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ActionRecenteDTO {
        private String id;
        private String action;
        private String utilisateur;
        private LocalDate horodatage;
        private String statut;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ProgressionParticipationDTO {
        private double tauxActuel;
        private double objectif;
        private double progression;
        private String tempsRestantEstime;
        private String tendance;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class WidgetProgressionDTO {
        private double pourcentageParticipation;
        private long nombreVotes;
        private long nombreElecteurs;
        private String statutProgression;
        private String couleurIndicateur;
        private String messageMotivation;
        private LocalDate horodatage;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class WidgetPodiumDTO {
        private VoteService.ResultatVoteDTO premierPlace;
        private VoteService.ResultatVoteDTO deuxiemePlace;
        private VoteService.ResultatVoteDTO troisiemePlace;
        private double ecartPremierDeuxieme;
        private String messageCompetition;
        private LocalDate horodatage;
    }
}