package com.personnal.electronicvoting.controller;

import com.personnal.electronicvoting.dto.VoteDTO;
import com.personnal.electronicvoting.service.VoteService;
import com.personnal.electronicvoting.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/votes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Vote", description = "APIs de gestion des votes")
public class VoteController {

    private final VoteService voteService;
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

    // ==================== PROCESSUS DE VOTE ====================

    /**
     * 🗳️ Effectuer un vote (endpoint principal)
     */
    @PostMapping("/effectuer")
    @Operation(summary = "Effectuer un vote",
            description = "Enregistrer le vote d'un électeur pour un candidat (un seul vote par électeur)")
    public ResponseEntity<VoteResponse> effectuerVote(
            @RequestHeader("Authorization") String token,
            @RequestParam String candidatId) {

        log.info("🗳️ Demande de vote - Candidat: {}", candidatId);

        try {
            var electeur = verifierEtObtenirElecteur(token);
            String electeurId = electeur.getExternalIdElecteur();

            // Effectuer le vote
            VoteDTO vote = voteService.effectuerVote(electeurId, candidatId);

            VoteResponse response = VoteResponse.builder()
                    .success(true)
                    .message("Vote enregistré avec succès !")
                    .electeurId(electeurId)
                    .candidatId(candidatId)
                    .dateVote(vote.getDateVote())
                    .build();

            log.info("✅ Vote enregistré avec succès - Électeur: {}, Candidat: {}",
                    electeurId, candidatId);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.warn("❌ Erreur lors du vote: {}", e.getMessage());

            VoteResponse response = VoteResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();

            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("💥 Erreur système lors du vote: {}", e.getMessage(), e);

            VoteResponse response = VoteResponse.builder()
                    .success(false)
                    .message("Erreur système. Veuillez réessayer.")
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ==================== CONSULTATION RÉSULTATS ====================

    /**
     * 📊 Consulter les résultats de l'élection
     */
    @GetMapping("/resultats")
    @Operation(summary = "Résultats de l'élection",
            description = "Obtenir les résultats complets de l'élection (accès public)")
    public ResponseEntity<List<VoteService.ResultatVoteDTO>> consulterResultats() {

        log.info("📊 Consultation des résultats de l'élection");

        try {
            List<VoteService.ResultatVoteDTO> resultats = voteService.obtenirResultatsVotes();
            return ResponseEntity.ok(resultats);

        } catch (Exception e) {
            log.error("💥 Erreur consultation résultats: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 🏆 Obtenir le candidat gagnant
     */
    @GetMapping("/gagnant")
    @Operation(summary = "Candidat gagnant",
            description = "Obtenir le candidat actuellement en tête")
    public ResponseEntity<VoteService.ResultatVoteDTO> obtenirGagnant() {

        log.info("🏆 Consultation du candidat gagnant");

        try {
            VoteService.ResultatVoteDTO gagnant = voteService.obtenirGagnant();
            return ResponseEntity.ok(gagnant);

        } catch (RuntimeException e) {
            log.warn("❌ Aucun gagnant disponible: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("💥 Erreur consultation gagnant: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== STATUT DE VOTE ====================

    /**
     * ✅ Vérifier le statut de vote d'un électeur
     */
    @GetMapping("/statut")
    @Operation(summary = "Statut de vote électeur",
            description = "Vérifier si l'électeur connecté a déjà voté")
    public ResponseEntity<VoteService.StatutVoteElecteurDTO> obtenirStatutVote(
            @RequestHeader("Authorization") String token) {

        log.info("✅ Consultation statut de vote");

        try {
            var electeur = verifierEtObtenirElecteur(token);
            String electeurId = electeur.getExternalIdElecteur();

            VoteService.StatutVoteElecteurDTO statut = voteService.obtenirStatutVoteElecteur(electeurId);
            return ResponseEntity.ok(statut);

        } catch (RuntimeException e) {
            log.warn("❌ Erreur statut vote: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("💥 Erreur système statut vote: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * ✅ Vérifier si un électeur peut voter
     */
    @GetMapping("/peut-voter")
    @Operation(summary = "Vérifier droit de vote",
            description = "Vérifier si l'électeur connecté peut encore voter")
    public ResponseEntity<PeutVoterResponse> verifierPeutVoter(
            @RequestHeader("Authorization") String token) {

        log.info("✅ Vérification droit de vote");

        try {
            var electeur = verifierEtObtenirElecteur(token);
            String electeurId = electeur.getExternalIdElecteur();

            boolean peutVoter = voteService.electeurPeutVoter(electeurId);

            PeutVoterResponse response = PeutVoterResponse.builder()
                    .peutVoter(peutVoter)
                    .message(peutVoter ?
                            "Vous pouvez voter" :
                            "Vous avez déjà voté")
                    .electeurId(electeurId)
                    .build();

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.warn("❌ Erreur vérification vote: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("💥 Erreur système vérification vote: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== STATISTIQUES ====================

    /**
     * 📊 Obtenir les statistiques globales de vote
     */
    @GetMapping("/statistiques")
    @Operation(summary = "Statistiques de vote",
            description = "Obtenir les statistiques globales de l'élection")
    public ResponseEntity<VoteService.StatistiquesVoteDTO> obtenirStatistiques() {

        log.info("📊 Consultation statistiques de vote");

        try {
            VoteService.StatistiquesVoteDTO stats = voteService.obtenirStatistiquesGenerales();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("💥 Erreur statistiques vote: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 📈 Obtenir la répartition temporelle des votes
     */
    @GetMapping("/repartition-temporelle")
    @Operation(summary = "Répartition temporelle",
            description = "Obtenir l'évolution temporelle des votes")
    public ResponseEntity<List<VoteService.VoteTemporelDTO>> obtenirRepartitionTemporelle() {

        log.info("📈 Consultation répartition temporelle des votes");

        try {
            List<VoteService.VoteTemporelDTO> repartition = voteService.obtenirRepartitionTemporelle();
            return ResponseEntity.ok(repartition);

        } catch (Exception e) {
            log.error("💥 Erreur répartition temporelle: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== ENDPOINTS DE VALIDATION ====================

    /**
     * 🔍 Prévisualiser son vote avant validation
     */
    @PostMapping("/previsualiser")
    @Operation(summary = "Prévisualiser vote",
            description = "Prévisualiser le vote avant confirmation (sans l'enregistrer)")
    public ResponseEntity<PreviewVoteResponse> previsualiserVote(
            @RequestHeader("Authorization") String token,
            @RequestParam String candidatId) {

        log.info("🔍 Prévisualisation vote - Candidat: {}", candidatId);

        try {
            var electeur = verifierEtObtenirElecteur(token);
            String electeurId = electeur.getExternalIdElecteur();

            // Vérifications sans enregistrer
            if (!voteService.electeurPeutVoter(electeurId)) {
                throw new RuntimeException("Vous avez déjà voté");
            }

            // Récupérer infos candidat
            // (Vous pouvez ajouter une méthode dans CandidatService si nécessaire)

            PreviewVoteResponse preview = PreviewVoteResponse.builder()
                    .electeurId(electeurId)
                    .electeurUsername(electeur.getUsername())
                    .candidatId(candidatId)
                    .messageConfirmation("Confirmez-vous votre vote pour ce candidat ?")
                    .avertissement("⚠️ Attention : Vous ne pourrez plus modifier votre vote après confirmation.")
                    .build();

            return ResponseEntity.ok(preview);

        } catch (RuntimeException e) {
            log.warn("❌ Erreur prévisualisation: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("💥 Erreur système prévisualisation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== DTOs DE RÉPONSE ====================

    /**
     * 🗳️ DTO pour réponse de vote
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class VoteResponse {
        private boolean success;
        private String message;
        private String electeurId;
        private String candidatId;
        private java.time.LocalDate dateVote;
    }

    /**
     * ✅ DTO pour vérification droit de vote
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PeutVoterResponse {
        private boolean peutVoter;
        private String message;
        private String electeurId;
    }

    /**
     * 🔍 DTO pour prévisualisation de vote
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PreviewVoteResponse {
        private String electeurId;
        private String electeurUsername;
        private String candidatId;
        private String messageConfirmation;
        private String avertissement;
    }
}