package com.personnal.electronicvoting.service;

import com.personnal.electronicvoting.dto.VoteDTO;
import com.personnal.electronicvoting.dto.CandidatDTO;
import com.personnal.electronicvoting.model.*;
import com.personnal.electronicvoting.repository.*;
import com.personnal.electronicvoting.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VoteService {

    private final VoteRepository voteRepository;
    private final ElecteurRepository electeurRepository;
    private final CandidatRepository candidatRepository;
    private final VoteMapper voteMapper;
    private final CandidatMapper candidatMapper;
    private final ElecteurService electeurService;

    // ==================== PROCESSUS DE VOTE ====================

    /**
     * 🗳️ Effectuer un vote (action principale)
     */
    public VoteDTO effectuerVote(String electeurId, String candidatId) {
        log.info("🗳️ Tentative de vote - Électeur: {}, Candidat: {}", electeurId, candidatId);

        try {
            // 🔒 VÉRIFICATIONS DE SÉCURITÉ

            // 1. Vérifier que l'électeur existe
            Electeur electeur = electeurRepository.findByExternalIdElecteur(electeurId)
                    .orElseThrow(() -> new RuntimeException("Électeur non trouvé: " + electeurId));

            // 2. Vérifier que le candidat existe
            Candidat candidat = candidatRepository.findByExternalIdCandidat(candidatId)
                    .orElseThrow(() -> new RuntimeException("Candidat non trouvé: " + candidatId));

            // 3. ⚠️ VÉRIFICATION CRUCIALE : L'électeur a-t-il déjà voté ?
            if (electeur.isAVote()) {
                log.warn("⚠️ Tentative de double vote - Électeur: {}", electeurId);
                throw new RuntimeException("Vous avez déjà voté. Un seul vote par électeur est autorisé.");
            }

            // 4. Vérifier en base si un vote existe (double sécurité)
            if (voteRepository.existsByElecteur_externalIdElecteur(electeurId)) {
                log.error("🚨 ALERTE SÉCURITÉ - Vote en base mais flag électeur incorrect: {}", electeurId);
                throw new RuntimeException("Anomalie détectée. Contactez l'administrateur.");
            }

            // ✅ ENREGISTREMENT DU VOTE

            Vote vote = new Vote();
            vote.setElecteur(electeur);
            vote.setCandidat(candidat);
            vote.setDateVote(LocalDateTime.now());

            Vote voteSauve = voteRepository.save(vote);

            // 🔄 MISE À JOUR ÉLECTEUR (marquer comme ayant voté)
            electeurService.marquerCommeAyantVote(electeurId);

            log.info("✅ Vote enregistré avec succès - Électeur: {}, Candidat: {}, ID Vote: {}",
                    electeurId, candidatId, voteSauve.getId());

            return voteMapper.toDTO(voteSauve);

        } catch (RuntimeException e) {
            log.error("❌ Erreur lors du vote - Électeur: {}, Candidat: {} - Erreur: {}",
                    electeurId, candidatId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("💥 Erreur système lors du vote - Électeur: {}, Candidat: {}",
                    electeurId, candidatId, e);
            throw new RuntimeException("Erreur système lors du vote. Veuillez réessayer.", e);
        }
    }

    // ==================== VÉRIFICATIONS ====================

    /**
     * ✅ Vérifier si un électeur peut voter
     */
    @Transactional(readOnly = true)
    public boolean electeurPeutVoter(String electeurId) {
        log.info("✅ Vérification droit de vote - Électeur: {}", electeurId);

        try {
            Electeur electeur = electeurRepository.findByExternalIdElecteur(electeurId)
                    .orElseThrow(() -> new RuntimeException("Électeur non trouvé"));

            boolean peutVoter = !electeur.isAVote() &&
                    !voteRepository.existsByElecteur_externalIdElecteur(electeurId);

            log.info("📊 Électeur {} - Peut voter: {}", electeurId, peutVoter);
            return peutVoter;

        } catch (Exception e) {
            log.error("💥 Erreur vérification droit vote: {}", e.getMessage());
            return false;
        }
    }

    /**
     * ✅ Vérifier si un électeur a déjà voté
     */
    @Transactional(readOnly = true)
    public boolean electeurAVote(String electeurId) {
        log.info("✅ Vérification si électeur a voté - Électeur: {}", electeurId);

        try {
            return voteRepository.existsByElecteur_externalIdElecteur(electeurId);
        } catch (Exception e) {
            log.error("💥 Erreur vérification vote existant: {}", e.getMessage());
            return false;
        }
    }

    // ==================== CONSULTATION RÉSULTATS ====================

    /**
     * 📊 Obtenir résultats complets des votes
     */
    @Transactional(readOnly = true)
    public List<ResultatVoteDTO> obtenirResultatsVotes() {
        log.info("📊 Calcul des résultats de vote");

        try {
            List<Candidat> tousLesCandidats = candidatRepository.findAll();
            long totalVotes = voteRepository.count();

            List<ResultatVoteDTO> resultats = tousLesCandidats.stream()
                    .map(candidat -> {
                        long votesCandidat = candidatRepository.countVotesByCandidat(candidat.getExternalIdCandidat());
                        double pourcentage = totalVotes > 0 ?
                                (double) votesCandidat / totalVotes * 100 : 0;

                        return ResultatVoteDTO.builder()
                                .candidat(candidatMapper.toDTO(candidat))
                                .nombreVotes(votesCandidat)
                                .pourcentageVotes(Math.round(pourcentage * 100.0) / 100.0)
                                .build();
                    })
                    .sorted((a, b) -> Long.compare(b.getNombreVotes(), a.getNombreVotes()))
                    .toList();

            // Attribuer les rangs
            for (int i = 0; i < resultats.size(); i++) {
                resultats.get(i).setRang(i + 1);
            }

            log.info("📊 Résultats calculés pour {} candidats avec {} votes total",
                    resultats.size(), totalVotes);

            return resultats;

        } catch (Exception e) {
            log.error("💥 Erreur calcul résultats: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors du calcul des résultats", e);
        }
    }

    /**
     * 🏆 Obtenir le candidat gagnant
     */
    @Transactional(readOnly = true)
    public ResultatVoteDTO obtenirGagnant() {
        log.info("🏆 Recherche du candidat gagnant");

        List<ResultatVoteDTO> resultats = obtenirResultatsVotes();

        if (resultats.isEmpty()) {
            throw new RuntimeException("Aucun résultat disponible");
        }

        ResultatVoteDTO gagnant = resultats.get(0);
        log.info("🏆 Gagnant: {} avec {} votes ({}%)",
                gagnant.getCandidat().getUsername(),
                gagnant.getNombreVotes(),
                gagnant.getPourcentageVotes());

        return gagnant;
    }

    // ==================== STATISTIQUES AVANCÉES ====================

    /**
     * 📊 Obtenir statistiques générales du vote
     */
    @Transactional(readOnly = true)
    public StatistiquesVoteDTO obtenirStatistiquesGenerales() {
        log.info("📊 Calcul statistiques générales de vote");

        try {
            long totalElecteurs = electeurRepository.count();
            long totalVotes = voteRepository.count();
            long electeursAyantVote = electeurRepository.findByaVoteTrue().size();
            long totalCandidats = candidatRepository.count();

            double tauxParticipation = totalElecteurs > 0 ?
                    (double) electeursAyantVote / totalElecteurs * 100 : 0;

            // Calculer votes par candidat
            Map<String, Long> votesParCandidat = candidatRepository.findAll()
                    .stream()
                    .collect(Collectors.toMap(
                            Candidat::getUsername,
                            candidat -> candidatRepository.countVotesByCandidat(candidat.getExternalIdCandidat())
                    ));

            return StatistiquesVoteDTO.builder()
                    .totalElecteurs(totalElecteurs)
                    .totalVotes(totalVotes)
                    .electeursAyantVote(electeursAyantVote)
                    .totalCandidats(totalCandidats)
                    .tauxParticipation(Math.round(tauxParticipation * 100.0) / 100.0)
                    .votesParCandidat(votesParCandidat)
                    .build();

        } catch (Exception e) {
            log.error("💥 Erreur calcul statistiques: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors du calcul des statistiques", e);
        }
    }

    /**
     * 📊 Obtenir répartition temporelle des votes
     */
    @Transactional(readOnly = true)
    public List<VoteTemporelDTO> obtenirRepartitionTemporelle() {
        log.info("📊 Analyse répartition temporelle des votes");

        try {
            List<Vote> tousLesVotes = voteRepository.findAll();

            Map<String, Long> votesParHeure = tousLesVotes.stream()
                    .collect(Collectors.groupingBy(
                            vote -> vote.getDateVote().toLocalDate().toString() + " " +
                                    vote.getDateVote().getHour() + "h",
                            Collectors.counting()
                    ));

            return votesParHeure.entrySet().stream()
                    .map(entry -> VoteTemporelDTO.builder()
                            .periode(entry.getKey())
                            .nombreVotes(entry.getValue())
                            .build())
                    .sorted((a, b) -> a.getPeriode().compareTo(b.getPeriode()))
                    .toList();

        } catch (Exception e) {
            log.error("💥 Erreur analyse temporelle: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'analyse temporelle", e);
        }
    }

    // ==================== DTOs SPÉCIFIQUES ====================

    /**
     * 📊 DTO pour résultats de vote
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ResultatVoteDTO {
        private CandidatDTO candidat;
        private long nombreVotes;
        private double pourcentageVotes;
        private int rang;
    }

    /**
     * 📊 DTO pour statistiques générales
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class StatistiquesVoteDTO {
        private long totalElecteurs;
        private long totalVotes;
        private long electeursAyantVote;
        private long totalCandidats;
        private double tauxParticipation;
        private Map<String, Long> votesParCandidat;
    }

    /**
     * 📊 DTO pour analyse temporelle
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class VoteTemporelDTO {
        private String periode;
        private long nombreVotes;
    }

    /**
     * ✅ DTO pour statut de vote d'un électeur
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class StatutVoteElecteurDTO {
        private String electeurId;
        private boolean aVote;
        private boolean peutVoter;
        private LocalDateTime dateVote; // null si n'a pas voté
        private String messageStatut;
    }

    /**
     * ✅ Obtenir statut de vote pour un électeur
     */
    @Transactional(readOnly = true)
    public StatutVoteElecteurDTO obtenirStatutVoteElecteur(String electeurId) {
        log.info("✅ Consultation statut vote électeur: {}", electeurId);

        try {
            Electeur electeur = electeurRepository.findByExternalIdElecteur(electeurId)
                    .orElseThrow(() -> new RuntimeException("Électeur non trouvé"));

            boolean aVote = electeur.isAVote();
            boolean peutVoter = !aVote;

            String message;
            LocalDateTime dateVote = null;

            if (aVote) {
                message = "Vous avez déjà voté. Merci pour votre participation !";
                // Récupérer la date du vote si nécessaire
                // dateVote = ... (requête pour récupérer la date)
            } else {
                message = "Vous pouvez voter. Consultez les candidats et campagnes.";
            }

            return StatutVoteElecteurDTO.builder()
                    .electeurId(electeurId)
                    .aVote(aVote)
                    .peutVoter(peutVoter)
                    .dateVote(dateVote)
                    .messageStatut(message)
                    .build();

        } catch (Exception e) {
            log.error("💥 Erreur consultation statut vote: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la consultation du statut", e);
        }
    }
}