package com.personnal.electronicvoting.service;

import com.personnal.electronicvoting.dto.CandidatDTO;
import com.personnal.electronicvoting.dto.CampagneDTO;
import com.personnal.electronicvoting.model.Candidat;
import com.personnal.electronicvoting.repository.*;
import com.personnal.electronicvoting.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CandidatService {

    private final CandidatRepository candidatRepository;
    private final CampagneRepository campagneRepository;
    private final VoteRepository voteRepository;
    private final CandidatMapper candidatMapper;
    private final CampagneMapper campagneMapper;

    // ==================== CONSULTATION PUBLIQUE ====================

    /**
     *  Lister tous les candidats (vue électeur)
     */
    public List<CandidatDTO> listerTousCandidats() {
        log.info(" Consultation publique - Liste des candidats");

        List<Candidat> candidats = candidatRepository.findAll();
        log.info("{} candidats trouvés", candidats.size());

        return candidats.stream()
                .map(candidatMapper::toDTO)
                .toList();
    }

    /**
     * Obtenir candidats classés par nombre de votes (résultats)
     */
    public List<CandidatAvecVotesDTO> obtenirClassementCandidats() {
        log.info(" Calcul classement des candidats par votes");

        List<Candidat> candidatsOrdonnes = candidatRepository.findAllOrderByVoteCountDesc();

        return candidatsOrdonnes.stream()
                .map(candidat -> {
                    long nombreVotes = candidatRepository.countVotesByCandidat(candidat.getExternalIdCandidat());
                    return CandidatAvecVotesDTO.builder()
                            .candidat(candidatMapper.toDTO(candidat))
                            .nombreVotes(nombreVotes)
                            .build();
                })
                .toList();
    }

    /**
     *  Trouver candidat par ID (vue électeur)
     */
    public CandidatDTO trouverCandidatParId(String externalId) {
        log.info(" Recherche candidat public: {}", externalId);

        return candidatRepository.findByExternalIdCandidat(externalId)
                .map(candidatMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé: " + externalId));
    }

    /**
     *  Rechercher candidats par nom (fonction recherche)
     */
    public List<CandidatDTO> rechercherCandidatsParNom(String nomPartiel) {
        log.info(" Recherche candidats par nom: '{}'", nomPartiel);

        if (nomPartiel == null || nomPartiel.trim().isEmpty()) {
            return listerTousCandidats();
        }

        List<Candidat> candidatsTrouves = candidatRepository.findByUsernameContaining(nomPartiel.trim());
        log.info(" {} candidats trouvés pour '{}'", candidatsTrouves.size(), nomPartiel);

        return candidatsTrouves.stream()
                .map(candidatMapper::toDTO)
                .toList();
    }

    // ==================== GESTION CAMPAGNES ====================

    /**
     *  Obtenir toutes les campagnes d'un candidat
     */
    public List<CampagneDTO> obtenirCampagnesCandidat(String candidatId) {
        log.info("Consultation campagnes du candidat: {}", candidatId);

        // Vérifier que le candidat existe
        candidatRepository.findByExternalIdCandidat(candidatId)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé: " + candidatId));

        List<com.personnal.electronicvoting.model.Campagne> campagnes =
                campagneRepository.findByCandidat_ExternalIdCandidat(candidatId);

        log.info(" {} campagnes trouvées pour le candidat {}", campagnes.size(), candidatId);

        return campagnes.stream()
                .map(campagneMapper::toDTO)
                .toList();
    }

    // ==================== STATISTIQUES CANDIDATS ====================

    /**
     *  Obtenir détails complets d'un candidat avec statistiques
     */
    public CandidatDetailDTO obtenirDetailCandidat(String candidatId) {
        log.info(" Consultation détails candidat: {}", candidatId);

        Candidat candidat = candidatRepository.findByExternalIdCandidat(candidatId)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé: " + candidatId));

        // Récupérer les campagnes
        List<com.personnal.electronicvoting.model.Campagne> campagnes =
                campagneRepository.findByCandidat_ExternalIdCandidat(candidatId);

        // Compter les votes
        long nombreVotes = candidatRepository.countVotesByCandidat(candidatId);

        return CandidatDetailDTO.builder()
                .candidat(candidatMapper.toDTO(candidat))
                .campagnes(campagnes.stream().map(campagneMapper::toDTO).toList())
                .nombreVotes(nombreVotes)
                .build();
    }

    /**
     * Obtenir le candidat en tête
     */
    public CandidatAvecVotesDTO obtenirCandidatEnTete() {
        log.info(" Recherche candidat en tête");

        List<CandidatAvecVotesDTO> classement = obtenirClassementCandidats();

        if (classement.isEmpty()) {
            throw new RuntimeException("Aucun candidat trouvé");
        }

        CandidatAvecVotesDTO premier = classement.get(0);
        log.info(" Candidat en tête: {} avec {} votes",
                premier.getCandidat().getUsername(), premier.getNombreVotes());

        return premier;
    }

    /**
     *  Vérifier si un candidat peut être supprimé (aucun vote)
     */
    public boolean peutEtreSupprime(String candidatId) {
        log.info(" Vérification suppression possible pour candidat: {}", candidatId);

        long nombreVotes = candidatRepository.countVotesByCandidat(candidatId);
        boolean supprimable = nombreVotes == 0;

        log.info(" Candidat {} - {} votes - Supprimable: {}",
                candidatId, nombreVotes, supprimable);

        return supprimable;
    }

    // ==================== DTOs SPÉCIFIQUES ====================

    /**
     *  DTO pour candidat avec nombre de votes
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CandidatAvecVotesDTO {
        private CandidatDTO candidat;
        private long nombreVotes;
    }

    /**
     *  DTO pour détails complets d'un candidat
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CandidatDetailDTO {
        private CandidatDTO candidat;
        private List<CampagneDTO> campagnes;
        private long nombreVotes;
    }

    /**
     *  DTO pour statistiques de campagne
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class StatistiquesCandidatDTO {
        private String candidatId;
        private String nomCandidat;
        private long nombreVotes;
        private int nombreCampagnes;
        private double pourcentageVotes;
        private int rang;
    }

    /**
     *  Obtenir statistiques détaillées de tous les candidats
     */
    public List<StatistiquesCandidatDTO> obtenirStatistiquesDetaillees() {
        log.info(" Calcul statistiques détaillées de tous les candidats");

        List<Candidat> tousLesCandidats = candidatRepository.findAll();
        long totalVotes = voteRepository.count();

        List<StatistiquesCandidatDTO> statistiques = tousLesCandidats.stream()
                .map(candidat -> {
                    long votesCandidat = candidatRepository.countVotesByCandidat(candidat.getExternalIdCandidat());
                    int nombreCampagnes = campagneRepository.findByCandidat_ExternalIdCandidat(
                            candidat.getExternalIdCandidat()).size();

                    double pourcentage = totalVotes > 0 ?
                            (double) votesCandidat / totalVotes * 100 : 0;

                    return StatistiquesCandidatDTO.builder()
                            .candidatId(candidat.getExternalIdCandidat())
                            .nomCandidat(candidat.getUsername())
                            .nombreVotes(votesCandidat)
                            .nombreCampagnes(nombreCampagnes)
                            .pourcentageVotes(Math.round(pourcentage * 100.0) / 100.0)
                            .build();
                })
                .sorted((a, b) -> Long.compare(b.getNombreVotes(), a.getNombreVotes()))
                .toList();

        // Attribuer les rangs
        for (int i = 0; i < statistiques.size(); i++) {
            statistiques.get(i).setRang(i + 1);
        }

        log.info(" Statistiques calculées pour {} candidats", statistiques.size());
        return statistiques;
    }
}