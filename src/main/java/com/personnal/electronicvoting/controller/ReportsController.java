package com.personnal.electronicvoting.controller;

import com.personnal.electronicvoting.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Rapports", description = "APIs de génération de rapports")
public class ReportsController {

    private final VoteService voteService;
    private final CandidatService candidatService;
    private final CampagneService campagneService;
    private final ElecteurService electeurService;
    private final AdministrateurService administrateurService;
    private final AuthService authService;

    // ==================== MIDDLEWARE SÉCURITÉ ====================

    private void verifierTokenAdmin(String token) {
        String cleanToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        if (!authService.verifierTokenAdmin(cleanToken)) {
            throw new RuntimeException("Token administrateur requis");
        }
    }

    // ==================== RAPPORTS ÉLECTORAUX ====================

    /**
     * 📊 Rapport complet des résultats
     */
    @GetMapping("/resultats-complets")
    @Operation(summary = "Rapport résultats complets",
            description = "Générer un rapport complet des résultats de l'élection")
    public ResponseEntity<RapportResultatsDTO> genererRapportResultats(
            @RequestHeader("Authorization") String token) {

        log.info("📊 Génération rapport résultats complets");

        try {
            verifierTokenAdmin(token);

            // Données principales
            List<VoteService.ResultatVoteDTO> resultats = voteService.obtenirResultatsVotes();
            VoteService.StatistiquesVoteDTO stats = voteService.obtenirStatistiquesGenerales();
            List<VoteService.VoteTemporelDTO> analyseTemporelle =
                    voteService.obtenirRepartitionTemporelle();

            // Analyses approfondies
            AnalyseResultatsDTO analyse = effectuerAnalyseResultats(resultats, stats);

            RapportResultatsDTO rapport = RapportResultatsDTO.builder()
                    .metadonnees(MetadonneesRapportDTO.builder()
                            .titre("Rapport Complet des Résultats")
                            .dateGeneration(LocalDateTime.now())
                            .auteur("Système Electoral")
                            .version("1.0")
                            .statut("FINAL")
                            .build())
                    .resumeExecutif(genererResumeExecutifResultats(resultats, stats))
                    .resultatsDetailles(resultats)
                    .statistiquesGlobales(stats)
                    .analyseTemporelle(analyseTemporelle)
                    .analysesApprofondies(analyse)
                    .conclusions(genererConclusions(resultats, stats))
                    .build();

            return ResponseEntity.ok(rapport);

        } catch (RuntimeException e) {
            log.warn("❌ Erreur génération rapport: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("💥 Erreur système rapport: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 👥 Rapport de participation
     */
    @GetMapping("/participation")
    @Operation(summary = "Rapport participation",
            description = "Rapport détaillé sur la participation électorale")
    public ResponseEntity<RapportParticipationDTO> genererRapportParticipation(
            @RequestHeader("Authorization") String token) {

        log.info("👥 Génération rapport participation");

        try {
            verifierTokenAdmin(token);

            AdministrateurService.StatistiquesAdminDTO statsAdmin =
                    administrateurService.obtenirStatistiques();
            VoteService.StatistiquesVoteDTO statsVote = voteService.obtenirStatistiquesGenerales();

            // Analyse démographique (simplifiée)
            AnalyseDemographiqueDTO analyseDemographique =
                    effectuerAnalyseDemographique(statsAdmin);

            // Évolution de la participation
            List<VoteService.VoteTemporelDTO> evolutionParticipation =
                    voteService.obtenirRepartitionTemporelle();

            RapportParticipationDTO rapport = RapportParticipationDTO.builder()
                    .metadonnees(MetadonneesRapportDTO.builder()
                            .titre("Rapport de Participation")
                            .dateGeneration(LocalDateTime.now())
                            .auteur("Système Electoral")
                            .version("1.0")
                            .statut("PROVISOIRE")
                            .build())
                    .tauxParticipationGlobal(statsVote.getTauxParticipation())
                    .nombreElecteursInscrits(statsAdmin.getTotalElecteurs())
                    .nombreVotants(statsAdmin.getElecteursAyantVote())
                    .analyseDemographique(analyseDemographique)
                    .evolutionTemporelle(evolutionParticipation)
                    .comparaisonObjectifs(genererComparaisonObjectifs(statsVote.getTauxParticipation()))
                    .facteurstParticipation(analyserFacteursParticipation())
                    .recommendations(genererRecommandationsParticipation(statsVote))
                    .build();

            return ResponseEntity.ok(rapport);

        } catch (RuntimeException e) {
            log.warn("❌ Erreur rapport participation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("💥 Erreur système rapport participation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== RAPPORTS CANDIDATS ====================

    /**
     * 🏆 Rapport analyse candidats
     */
    @GetMapping("/candidats")
    @Operation(summary = "Rapport candidats",
            description = "Analyse complète des candidats et leurs performances")
    public ResponseEntity<RapportCandidatsDTO> genererRapportCandidats(
            @RequestHeader("Authorization") String token) {

        log.info("🏆 Génération rapport candidats");

        try {
            verifierTokenAdmin(token);

            List<CandidatService.StatistiquesCandidatDTO> statsCandidats =
                    candidatService.obtenirStatistiquesDetaillees();
            List<VoteService.ResultatVoteDTO> resultats = voteService.obtenirResultatsVotes();

            // Analyses spécialisées
            AnalyseConcurrenceDTO analyseConcurrence = effectuerAnalyseConcurrence(resultats);
            AnalysePerformanceDTO analysePerformance =
                    effectuerAnalysePerformance(statsCandidats);

            RapportCandidatsDTO rapport = RapportCandidatsDTO.builder()
                    .metadonnees(MetadonneesRapportDTO.builder()
                            .titre("Rapport d'Analyse des Candidats")
                            .dateGeneration(LocalDateTime.now())
                            .auteur("Système Electoral")
                            .version("1.0")
                            .statut("ANALYSE")
                            .build())
                    .nombreTotalCandidats(statsCandidats.size())
                    .statistiquesIndividuelles(statsCandidats)
                    .classementGeneral(resultats)
                    .analyseConcurrence(analyseConcurrence)
                    .analysePerformance(analysePerformance)
                    .tendancesObservees(identifierTendancesCandidats(statsCandidats))
                    .profilsTypes(genererProfilsTypesCandidats(statsCandidats))
                    .build();

            return ResponseEntity.ok(rapport);

        } catch (RuntimeException e) {
            log.warn("❌ Erreur rapport candidats: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("💥 Erreur système rapport candidats: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== RAPPORTS CAMPAGNES ====================

    /**
     * 📢 Rapport efficacité campagnes
     */
    @GetMapping("/campagnes")
    @Operation(summary = "Rapport campagnes",
            description = "Analyse de l'efficacité des campagnes électorales")
    public ResponseEntity<RapportCampagnesDTO> genererRapportCampagnes(
            @RequestHeader("Authorization") String token) {

        log.info("📢 Génération rapport campagnes");

        try {
            verifierTokenAdmin(token);

            CampagneService.StatistiquesCampagnesDTO statsCampagnes =
                    campagneService.obtenirStatistiquesCampagnes();
            List<CampagneService.RepartitionCampagnesDTO> repartition =
                    campagneService.obtenirRepartitionParCandidat();

            // Analyse d'efficacité
            AnalyseEfficaciteCampagnesDTO analyseEfficacite =
                    effectuerAnalyseEfficaciteCampagnes(repartition);

            RapportCampagnesDTO rapport = RapportCampagnesDTO.builder()
                    .metadonnees(MetadonneesRapportDTO.builder()
                            .titre("Rapport d'Efficacité des Campagnes")
                            .dateGeneration(LocalDateTime.now())
                            .auteur("Système Electoral")
                            .version("1.0")
                            .statut("ANALYSE")
                            .build())
                    .statistiquesGlobales(statsCampagnes)
                    .repartitionParCandidat(repartition)
                    .analyseEfficacite(analyseEfficacite)
                    .meilleuresPratiques(identifierMeilleuresPratiques(repartition))
                    .recommandationsAmelioration(genererRecommandationsCampagnes())
                    .build();

            return ResponseEntity.ok(rapport);

        } catch (RuntimeException e) {
            log.warn("❌ Erreur rapport campagnes: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("💥 Erreur système rapport campagnes: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== EXPORT FORMATS ====================

    /**
     * 📄 Export rapport en CSV
     */
    @GetMapping("/export/csv/{typeRapport}")
    @Operation(summary = "Export CSV",
            description = "Exporter un rapport au format CSV")
    public ResponseEntity<String> exporterRapportCSV(
            @RequestHeader("Authorization") String token,
            @PathVariable String typeRapport) {

        log.info("📄 Export CSV - Type: {}", typeRapport);

        try {
            verifierTokenAdmin(token);

            String csvContent = switch (typeRapport.toLowerCase()) {
                case "resultats" -> genererCSVResultats();
                case "participation" -> genererCSVParticipation();
                case "candidats" -> genererCSVCandidats();
                case "campagnes" -> genererCSVCampagnes();
                default -> throw new RuntimeException("Type de rapport non supporté: " + typeRapport);
            };

            String filename = String.format("rapport_%s_%s.csv",
                    typeRapport,
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm")));

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(csvContent);

        } catch (RuntimeException e) {
            log.warn("❌ Erreur export CSV: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        } catch (Exception e) {
            log.error("💥 Erreur système export CSV: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur système");
        }
    }

    /**
     * 📊 Export rapport exécutif consolidé
     */
    @GetMapping("/executif")
    @Operation(summary = "Rapport exécutif",
            description = "Rapport consolidé pour la direction")
    public ResponseEntity<RapportExecutifDTO> genererRapportExecutif(
            @RequestHeader("Authorization") String token) {

        log.info("📊 Génération rapport exécutif");

        try {
            verifierTokenAdmin(token);

            // Agrégation de toutes les données
            VoteService.StatistiquesVoteDTO statsVote = voteService.obtenirStatistiquesGenerales();
            AdministrateurService.StatistiquesAdminDTO statsAdmin =
                    administrateurService.obtenirStatistiques();
            List<VoteService.ResultatVoteDTO> resultats = voteService.obtenirResultatsVotes();

            // Indicateurs clés de performance
            IndicateursClesToDTO indicateurs = calculerIndicateursClesToutes(
                    statsVote, statsAdmin, resultats);

            // Synthèse exécutive
            SyntheseExecutiveDTO synthese = genererSyntheseExecutive(
                    statsVote, statsAdmin, resultats);

            // Risques et opportunités
            AnalyseRisquesOpportunitesDTO risquesOpportunites =
                    analyserRisquesOpportunites(statsVote, resultats);

            RapportExecutifDTO rapport = RapportExecutifDTO.builder()
                    .metadonnees(MetadonneesRapportDTO.builder()
                            .titre("Rapport Exécutif - Élection")
                            .dateGeneration(LocalDateTime.now())
                            .auteur("Direction Electoral")
                            .version("1.0")
                            .statut("CONFIDENTIEL")
                            .build())
                    .syntheseExecutive(synthese)
                    .indicateursClesToutes(indicateurs)
                    .principauxResultats(resultats.stream().limit(3).toList())
                    .risquesOpportunites(risquesOpportunites)
                    .recommandationsStrategiques(genererRecommandationsStrategiques())
                    .prochainesEtapes(genererProchainesTops())
                    .build();

            return ResponseEntity.ok(rapport);

        } catch (RuntimeException e) {
            log.warn("❌ Erreur rapport exécutif: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("💥 Erreur système rapport exécutif: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    private AnalyseResultatsDTO effectuerAnalyseResultats(
            List<VoteService.ResultatVoteDTO> resultats,
            VoteService.StatistiquesVoteDTO stats) {

        boolean competitionSerree = resultats.size() > 1 &&
                resultats.get(0).getNombreVotes() - resultats.get(1).getNombreVotes() < 10;

        return AnalyseResultatsDTO.builder()
                .competitionSerree(competitionSerree)
                .ecartMaximal(resultats.isEmpty() ? 0 :
                        resultats.get(0).getNombreVotes() -
                                resultats.get(resultats.size() - 1).getNombreVotes())
                .tendanceGenerale(stats.getTauxParticipation() > 50 ? "POSITIVE" : "MODERATE")
                .facteursDeterminants(List.of("Participation", "Campagnes", "Mobilisation"))
                .build();
    }

    private String genererResumeExecutifResultats(
            List<VoteService.ResultatVoteDTO> resultats,
            VoteService.StatistiquesVoteDTO stats) {
        if (resultats.isEmpty()) {
            return "Aucun vote enregistré pour le moment.";
        }

        var gagnant = resultats.get(0);
        return String.format(
                "L'élection enregistre un taux de participation de %.1f%% avec %d votes exprimés. " +
                        "%s mène avec %d votes (%.1f%%). La compétition %s.",
                stats.getTauxParticipation(),
                stats.getTotalVotes(),
                gagnant.getCandidat().getUsername(),
                gagnant.getNombreVotes(),
                gagnant.getPourcentageVotes(),
                resultats.size() > 1 &&
                        gagnant.getNombreVotes() - resultats.get(1).getNombreVotes() < 10 ?
                        "reste serrée" : "montre une avance claire"
        );
    }

    private List<String> genererConclusions(
            List<VoteService.ResultatVoteDTO> resultats,
            VoteService.StatistiquesVoteDTO stats) {
        List<String> conclusions = new java.util.ArrayList<>();

        conclusions.add("La participation de " + stats.getTauxParticipation() + "% reflète " +
                (stats.getTauxParticipation() > 60 ? "un engagement fort" : "une mobilisation modérée"));

        if (!resultats.isEmpty()) {
            conclusions.add("Le classement actuel montre " +
                    (resultats.size() > 1 ? "une compétition dynamique" : "un candidat unique"));
        }

        conclusions.add("Le processus électoral se déroule de manière transparente et sécurisée");

        return conclusions;
    }

    // Méthodes de génération CSV
    private String genererCSVResultats() {
        try {
            List<VoteService.ResultatVoteDTO> resultats = voteService.obtenirResultatsVotes();
            StringBuilder csv = new StringBuilder();
            csv.append("Rang,Candidat,Votes,Pourcentage\n");

            for (VoteService.ResultatVoteDTO resultat : resultats) {
                csv.append(String.format("%d,%s,%d,%.2f\n",
                        resultat.getRang(),
                        resultat.getCandidat().getUsername(),
                        resultat.getNombreVotes(),
                        resultat.getPourcentageVotes()));
            }

            return csv.toString();
        } catch (Exception e) {
            log.error("Erreur génération CSV résultats: {}", e.getMessage());
            return "Erreur lors de la génération du CSV";
        }
    }

    private String genererCSVParticipation() {
        try {
            VoteService.StatistiquesVoteDTO stats = voteService.obtenirStatistiquesGenerales();
            StringBuilder csv = new StringBuilder();
            csv.append("Métrique,Valeur\n");
            csv.append(String.format("Total Électeurs,%d\n", stats.getTotalElecteurs()));
            csv.append(String.format("Électeurs ayant voté,%d\n", stats.getElecteursAyantVote()));
            csv.append(String.format("Taux de participation,%.2f%%\n", stats.getTauxParticipation()));

            return csv.toString();
        } catch (Exception e) {
            log.error("Erreur génération CSV participation: {}", e.getMessage());
            return "Erreur lors de la génération du CSV";
        }
    }

    private String genererCSVCandidats() {
        try {
            List<CandidatService.StatistiquesCandidatDTO> stats =
                    candidatService.obtenirStatistiquesDetaillees();
            StringBuilder csv = new StringBuilder();
            csv.append("Candidat,Votes,Campagnes,Pourcentage,Rang\n");

            for (CandidatService.StatistiquesCandidatDTO stat : stats) {
                csv.append(String.format("%s,%d,%d,%.2f,%d\n",
                        stat.getNomCandidat(),
                        stat.getNombreVotes(),
                        stat.getNombreCampagnes(),
                        stat.getPourcentageVotes(),
                        stat.getRang()));
            }

            return csv.toString();
        } catch (Exception e) {
            log.error("Erreur génération CSV candidats: {}", e.getMessage());
            return "Erreur lors de la génération du CSV";
        }
    }

    private String genererCSVCampagnes() {
        try {
            CampagneService.StatistiquesCampagnesDTO stats =
                    campagneService.obtenirStatistiquesCampagnes();
            StringBuilder csv = new StringBuilder();
            csv.append("Métrique,Valeur\n");
            csv.append(String.format("Total Campagnes,%d\n", stats.getTotalCampagnes()));
            csv.append(String.format("Campagnes avec photos,%d\n", stats.getCampagnesAvecPhotos()));
            csv.append(String.format("Candidats avec campagnes,%d\n", stats.getCandidatsAvecCampagnes()));

            return csv.toString();
        } catch (Exception e) {
            log.error("Erreur génération CSV campagnes: {}", e.getMessage());
            return "Erreur lors de la génération du CSV";
        }
    }

    // Méthodes d'analyse (simplifiées pour l'exemple)
    private AnalyseDemographiqueDTO effectuerAnalyseDemographique(
            AdministrateurService.StatistiquesAdminDTO stats) {
        return AnalyseDemographiqueDTO.builder()
                .totalElecteurs(stats.getTotalElecteurs())
                .electeursActifs(stats.getElecteursAyantVote())
                .tauxActivation(stats.getTauxParticipation())
                .tendanceEvolution("STABLE")
                .build();
    }

    private ComparaisonObjectifsDTO genererComparaisonObjectifs(double tauxActuel) {
        return ComparaisonObjectifsDTO.builder()
                .objectifParticipation(75.0)
                .tauxActuel(tauxActuel)
                .ecart(tauxActuel - 75.0)
                .statutObjectif(tauxActuel >= 75.0 ? "ATTEINT" : "EN_COURS")
                .build();
    }

    private List<String> analyserFacteursParticipation() {
        return List.of(
                "Facilité d'accès à la plateforme",
                "Qualité des campagnes",
                "Intérêt pour les candidats",
                "Communication sur l'élection"
        );
    }

    private List<String> genererRecommandationsParticipation(
            VoteService.StatistiquesVoteDTO stats) {
        List<String> recommandations = new java.util.ArrayList<>();

        if (stats.getTauxParticipation() < 50) {
            recommandations.add("Renforcer la communication sur l'importance du vote");
            recommandations.add("Simplifier davantage le processus de vote");
        }

        recommandations.add("Maintenir la transparence des résultats");
        recommandations.add("Encourager le partage d'expérience entre électeurs");

        return recommandations;
    }

    // Autres méthodes d'analyse (simplifiées)
    private AnalyseConcurrenceDTO effectuerAnalyseConcurrence(
            List<VoteService.ResultatVoteDTO> resultats) {
        return AnalyseConcurrenceDTO.builder()
                .nombreConcurrents(resultats.size())
                .competitionIntense(resultats.size() > 1 &&
                        resultats.get(0).getNombreVotes() - resultats.get(1).getNombreVotes() < 20)
                .candidatDominant(resultats.isEmpty() ? null : resultats.get(0).getCandidat().getUsername())
                .build();
    }

    private AnalysePerformanceDTO effectuerAnalysePerformance(
            List<CandidatService.StatistiquesCandidatDTO> stats) {
        return AnalysePerformanceDTO.builder()
                .candidatLePlusActif(stats.stream()
                        .max((a, b) -> Integer.compare(a.getNombreCampagnes(), b.getNombreCampagnes()))
                        .map(CandidatService.StatistiquesCandidatDTO::getNomCandidat)
                        .orElse("Aucun"))
                .moyenneCampagnesParCandidat(stats.stream()
                        .mapToInt(CandidatService.StatistiquesCandidatDTO::getNombreCampagnes)
                        .average()
                        .orElse(0))
                .build();
    }

    private List<String> identifierTendancesCandidats(
            List<CandidatService.StatistiquesCandidatDTO> stats) {
        return List.of(
                "Corrélation positive entre nombre de campagnes et votes",
                "Importance de la qualité des campagnes",
                "Nécessité d'une présence active"
        );
    }

    private List<String> genererProfilsTypesCandidats(
            List<CandidatService.StatistiquesCandidatDTO> stats) {
        return List.of(
                "Candidat actif (nombreuses campagnes)",
                "Candidat efficace (peu de campagnes, beaucoup de votes)",
                "Candidat en développement (campagnes en cours)"
        );
    }

    private AnalyseEfficaciteCampagnesDTO effectuerAnalyseEfficaciteCampagnes(
            List<CampagneService.RepartitionCampagnesDTO> repartition) {
        return AnalyseEfficaciteCampagnesDTO.builder()
                .candidatLePlusActif(repartition.stream()
                        .max((a, b) -> Integer.compare(a.getNombreCampagnes(), b.getNombreCampagnes()))
                        .map(CampagneService.RepartitionCampagnesDTO::getNomCandidat)
                        .orElse("Aucun"))
                .moyenneCampagnesParCandidat(repartition.stream()
                        .mapToInt(CampagneService.RepartitionCampagnesDTO::getNombreCampagnes)
                        .average()
                        .orElse(0))
                .build();
    }

    private List<String> identifierMeilleuresPratiques(
            List<CampagneService.RepartitionCampagnesDTO> repartition) {
        return List.of(
                "Multiplier les campagnes pour augmenter la visibilité",
                "Utiliser des visuels attractifs",
                "Adapter le message selon l'audience"
        );
    }

    private List<String> genererRecommandationsCampagnes() {
        return List.of(
                "Encourager l'utilisation de photos dans les campagnes",
                "Proposer des modèles de campagnes efficaces",
                "Analyser l'impact des différents types de contenu"
        );
    }

    private IndicateursClesToDTO calculerIndicateursClesToutes(
            VoteService.StatistiquesVoteDTO statsVote,
            AdministrateurService.StatistiquesAdminDTO statsAdmin,
            List<VoteService.ResultatVoteDTO> resultats) {
        return IndicateursClesToDTO.builder()
                .tauxParticipation(statsVote.getTauxParticipation())
                .nombreCandidats(statsAdmin.getTotalCandidats())
                .nombreCampagnes(statsAdmin.getTotalCampagnes())
                .competitivite(resultats.size() > 1 ? "ELEVEE" : "MODERATE")
                .engagementElectoral(statsVote.getTauxParticipation() > 60 ? "FORT" : "MODERE")
                .build();
    }

    private SyntheseExecutiveDTO genererSyntheseExecutive(
            VoteService.StatistiquesVoteDTO statsVote,
            AdministrateurService.StatistiquesAdminDTO statsAdmin,
            List<VoteService.ResultatVoteDTO> resultats) {
        return SyntheseExecutiveDTO.builder()
                .situationGlobale("L'élection se déroule normalement avec une participation de " +
                        statsVote.getTauxParticipation() + "%")
                .principauxDefis(List.of("Maintenir l'engagement", "Assurer la transparence"))
                .realisationsCles(List.of("Processus sécurisé", "Transparence temps réel"))
                .perspectivesAvenir("Finalisation prévue selon planning")
                .build();
    }

    private AnalyseRisquesOpportunitesDTO analyserRisquesOpportunites(
            VoteService.StatistiquesVoteDTO stats,
            List<VoteService.ResultatVoteDTO> resultats) {
        return AnalyseRisquesOpportunitesDTO.builder()
                .risquesIdentifies(stats.getTauxParticipation() < 40 ?
                        List.of("Participation faible") : List.of())
                .opportunites(List.of("Transparence élevée", "Processus moderne"))
                .mesuresCorrectivesMises(List.of("Monitoring temps réel", "Support utilisateur"))
                .build();
    }

    private List<String> genererRecommandationsStrategiques() {
        return List.of(
                "Maintenir la communication transparente",
                "Renforcer l'accompagnement des électeurs",
                "Capitaliser sur la technologie pour les futures élections"
        );
    }

    private List<String> genererProchainesTops() {
        return List.of(
                "Finaliser le processus électoral",
                "Préparer l'annonce officielle des résultats",
                "Effectuer le bilan post-électoral",
                "Planifier les améliorations futures"
        );
    }

    // ==================== DTOs DE RAPPORTS ====================

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MetadonneesRapportDTO {
        private String titre;
        private LocalDateTime dateGeneration;
        private String auteur;
        private String version;
        private String statut;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RapportResultatsDTO {
        private MetadonneesRapportDTO metadonnees;
        private String resumeExecutif;
        private List<VoteService.ResultatVoteDTO> resultatsDetailles;
        private VoteService.StatistiquesVoteDTO statistiquesGlobales;
        private List<VoteService.VoteTemporelDTO> analyseTemporelle;
        private AnalyseResultatsDTO analysesApprofondies;
        private List<String> conclusions;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AnalyseResultatsDTO {
        private boolean competitionSerree;
        private long ecartMaximal;
        private String tendanceGenerale;
        private List<String> facteursDeterminants;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RapportParticipationDTO {
        private MetadonneesRapportDTO metadonnees;
        private double tauxParticipationGlobal;
        private long nombreElecteursInscrits;
        private long nombreVotants;
        private AnalyseDemographiqueDTO analyseDemographique;
        private List<VoteService.VoteTemporelDTO> evolutionTemporelle;
        private ComparaisonObjectifsDTO comparaisonObjectifs;
        private List<String> facteurstParticipation;
        private List<String> recommendations;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AnalyseDemographiqueDTO {
        private long totalElecteurs;
        private long electeursActifs;
        private double tauxActivation;
        private String tendanceEvolution;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ComparaisonObjectifsDTO {
        private double objectifParticipation;
        private double tauxActuel;
        private double ecart;
        private String statutObjectif;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RapportCandidatsDTO {
        private MetadonneesRapportDTO metadonnees;
        private int nombreTotalCandidats;
        private List<CandidatService.StatistiquesCandidatDTO> statistiquesIndividuelles;
        private List<VoteService.ResultatVoteDTO> classementGeneral;
        private AnalyseConcurrenceDTO analyseConcurrence;
        private AnalysePerformanceDTO analysePerformance;
        private List<String> tendancesObservees;
        private List<String> profilsTypes;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AnalyseConcurrenceDTO {
        private int nombreConcurrents;
        private boolean competitionIntense;
        private String candidatDominant;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AnalysePerformanceDTO {
        private String candidatLePlusActif;
        private double moyenneCampagnesParCandidat;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RapportCampagnesDTO {
        private MetadonneesRapportDTO metadonnees;
        private CampagneService.StatistiquesCampagnesDTO statistiquesGlobales;
        private List<CampagneService.RepartitionCampagnesDTO> repartitionParCandidat;
        private AnalyseEfficaciteCampagnesDTO analyseEfficacite;
        private List<String> meilleuresPratiques;
        private List<String> recommandationsAmelioration;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AnalyseEfficaciteCampagnesDTO {
        private String candidatLePlusActif;
        private double moyenneCampagnesParCandidat;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RapportExecutifDTO {
        private MetadonneesRapportDTO metadonnees;
        private SyntheseExecutiveDTO syntheseExecutive;
        private IndicateursClesToDTO indicateursClesToutes;
        private List<VoteService.ResultatVoteDTO> principauxResultats;
        private AnalyseRisquesOpportunitesDTO risquesOpportunites;
        private List<String> recommandationsStrategiques;
        private List<String> prochainesEtapes;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SyntheseExecutiveDTO {
        private String situationGlobale;
        private List<String> principauxDefis;
        private List<String> realisationsCles;
        private String perspectivesAvenir;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class IndicateursClesToDTO {
        private double tauxParticipation;
        private long nombreCandidats;
        private long nombreCampagnes;
        private String competitivite;
        private String engagementElectoral;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AnalyseRisquesOpportunitesDTO {
        private List<String> risquesIdentifies;
        private List<String> opportunites;
        private List<String> mesuresCorrectivesMises;
    }
}