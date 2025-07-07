package com.personnal.electronicvoting.service;

import com.personnal.electronicvoting.dto.ElecteurDTO;
import com.personnal.electronicvoting.dto.CandidatDTO;
import com.personnal.electronicvoting.dto.CampagneDTO;
import com.personnal.electronicvoting.dto.request.ChangePasswordRequest;
import com.personnal.electronicvoting.model.Electeur;
import com.personnal.electronicvoting.repository.*;
import com.personnal.electronicvoting.mapper.*;
import com.personnal.electronicvoting.util.PasswordGenerator;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ElecteurService {

    private final ElecteurRepository electeurRepository;
    private final CandidatRepository candidatRepository;
    private final CampagneRepository campagneRepository;
    private final VoteRepository voteRepository;
    private final UserMapper userMapper;
    private final CandidatMapper candidatMapper;
    private final CampagneMapper campagneMapper;
    private final PasswordEncoder passwordEncoder;
    private final PasswordGenerator passwordGenerator;
    private final EmailService emailService;


    /**
     * Obtenir profil électeur
     */
    @Transactional(readOnly = true)
    public ElecteurProfilDTO obtenirProfil(String electeurId) {
        log.info("👤 Consultation profil électeur: {}", electeurId);

        try {
            Electeur electeur = electeurRepository.findByExternalIdElecteur(electeurId)
                    .orElseThrow(() -> new RuntimeException("Électeur non trouvé"));

            // Calculer statistiques personnelles
            boolean aVote = electeur.isAVote();
            long totalElecteurs = electeurRepository.count();
            long electeursAyantVote = electeurRepository.findByaVoteTrue().size();
            double tauxParticipation = totalElecteurs > 0 ?
                    (double) electeursAyantVote / totalElecteurs * 100 : 0;

            return ElecteurProfilDTO.builder()
                    .electeur(userMapper.toDTO(electeur))
                    .aVote(aVote)
                    .dateInscription(LocalDateTime.now())
                    .nombreTotalElecteurs(totalElecteurs)
                    .tauxParticipationGlobal(Math.round(tauxParticipation * 100.0) / 100.0)
                    .messageStatut(aVote ?
                            "Vous avez participé au vote. Merci !" :
                            "Vous n'avez pas encore voté.")
                    .build();

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("💥 Erreur consultation profil: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la consultation du profil", e);
        }
    }

    /**
     * 🔑 Changer mot de passe électeur
     */
    public void changerMotDePasse(String electeurId, ChangePasswordRequest request) {
        log.info("🔑 Changement mot de passe électeur: {}", electeurId);

        try {
            Electeur electeur = electeurRepository.findByExternalIdElecteur(electeurId)
                    .orElseThrow(() -> new RuntimeException("Électeur non trouvé"));

            // Vérifier ancien mot de passe
            if (!passwordEncoder.matches(request.getAncienMotDePasse(), electeur.getMotDePasse())) {
                throw new RuntimeException("Ancien mot de passe incorrect");
            }

            // Changer le mot de passe
            electeur.setMotDePasse(passwordEncoder.encode(request.getNouveauMotDePasse()));
            electeurRepository.save(electeur);

            log.info("✅ Mot de passe changé avec succès pour électeur: {}", electeurId);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("💥 Erreur changement mot de passe: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors du changement de mot de passe", e);
        }
    }

    // ==================== CONSULTATION ÉLECTORALE ====================

    /**
     * 🏆 Consulter liste des candidats (vue électeur)
     */
    @Transactional(readOnly = true)
    public List<CandidatAvecStatutDTO> consulterCandidats(String electeurId) {
        log.info("🏆 Électeur {} consulte la liste des candidats", electeurId);

        try {
            // Vérifier que l'électeur existe
            electeurRepository.findByExternalIdElecteur(electeurId)
                    .orElseThrow(() -> new RuntimeException("Électeur non trouvé"));

            return candidatRepository.findAll()
                    .stream()
                    .map(candidat -> {
                        long nombreVotes = candidatRepository.countVotesByCandidat(candidat.getExternalIdCandidat());
                        int nombreCampagnes = campagneRepository.findByCandidat_ExternalIdCandidat(
                                candidat.getExternalIdCandidat()).size();

                        return CandidatAvecStatutDTO.builder()
                                .candidat(candidatMapper.toDTO(candidat))
                                .nombreVotes(nombreVotes)
                                .nombreCampagnes(nombreCampagnes)
                                .build();
                    })
                    .toList();

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("💥 Erreur consultation candidats: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la consultation des candidats", e);
        }
    }

    /**
     * 📢 Consulter campagnes d'un candidat
     */
    @Transactional(readOnly = true)
    public List<CampagneDTO> consulterCampagnesCandidat(String electeurId, String candidatId) {
        log.info("📢 Électeur {} consulte campagnes du candidat {}", electeurId, candidatId);

        try {
            // Vérifier que l'électeur existe
            electeurRepository.findByExternalIdElecteur(electeurId)
                    .orElseThrow(() -> new RuntimeException("Électeur non trouvé"));

            // Vérifier que le candidat existe
            candidatRepository.findByExternalIdCandidat(candidatId)
                    .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

            return campagneRepository.findByCandidat_ExternalIdCandidat(candidatId)
                    .stream()
                    .map(campagneMapper::toDTO)
                    .toList();

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("💥 Erreur consultation campagnes: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la consultation des campagnes", e);
        }
    }

    /**
     * 📊 Consulter résultats partiels (si autorisé)
     */
    @Transactional(readOnly = true)
    public ResultatsPartielsDTO consulterResultatsPartiels(String electeurId) {
        log.info("📊 Électeur {} consulte les résultats partiels", electeurId);

        try {
            Electeur electeur = electeurRepository.findByExternalIdElecteur(electeurId)
                    .orElseThrow(() -> new RuntimeException("Électeur non trouvé"));


            long totalVotes = voteRepository.count();
            long totalElecteurs = electeurRepository.count();
            double tauxParticipation = totalElecteurs > 0 ?
                    (double) totalVotes / totalElecteurs * 100 : 0;

            List<ResultatCandidatDTO> resultats = candidatRepository.findAll()
                    .stream()
                    .map(candidat -> {
                        long votes = candidatRepository.countVotesByCandidat(candidat.getExternalIdCandidat());
                        double pourcentage = totalVotes > 0 ? (double) votes / totalVotes * 100 : 0;

                        return ResultatCandidatDTO.builder()
                                .nomCandidat(candidat.getUsername())
                                .nombreVotes(votes)
                                .pourcentageVotes(Math.round(pourcentage * 100.0) / 100.0)
                                .build();
                    })
                    .sorted((a, b) -> Long.compare(b.getNombreVotes(), a.getNombreVotes()))
                    .toList();

            return ResultatsPartielsDTO.builder()
                    .totalVotes(totalVotes)
                    .totalElecteurs(totalElecteurs)
                    .tauxParticipation(Math.round(tauxParticipation * 100.0) / 100.0)
                    .resultatsParCandidat(resultats)
                    .build();

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("💥 Erreur consultation résultats: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la consultation des résultats", e);
        }
    }

    // ==================== MÉTHODES EXISTANTES CONSERVÉES ====================

    /**
     * 📋 Lister tous les électeurs (usage admin)
     */
    @Transactional(readOnly = true)
    public List<ElecteurDTO> listerTous() {
        log.info("📋 Liste de tous les électeurs");
        return electeurRepository.findAll()
                .stream()
                .map(userMapper::toDTO)
                .toList();
    }

    /**
     * ✅ Marquer électeur comme ayant voté
     */
    public void marquerCommeAyantVote(String externalId) {
        log.info("✅ Marquage électeur ayant voté: {}", externalId);

        try {
            Electeur electeur = electeurRepository.findByExternalIdElecteur(externalId)
                    .orElseThrow(() -> new RuntimeException("Électeur non trouvé"));
            electeur.setAVote(true);
            electeurRepository.save(electeur);

            log.info("✅ Électeur {} marqué comme ayant voté", externalId);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("💥 Erreur marquage vote: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors du marquage", e);
        }
    }

    /**
     * 🔍 Trouver électeur par ID
     */
    @Transactional(readOnly = true)
    public Optional<ElecteurDTO> trouverParExternalId(String externalId) {
        log.info("🔍 Recherche électeur: {}", externalId);
        return electeurRepository.findByExternalIdElecteur(externalId)
                .map(userMapper::toDTO);
    }

    // ==================== TABLEAU DE BORD ÉLECTEUR ====================

    /**
     * 📊 Obtenir tableau de bord électeur
     */
    @Transactional(readOnly = true)
    public TableauBordElecteurDTO obtenirTableauBord(String electeurId) {
        log.info("📊 Génération tableau de bord électeur: {}", electeurId);

        try {
            Electeur electeur = electeurRepository.findByExternalIdElecteur(electeurId)
                    .orElseThrow(() -> new RuntimeException("Électeur non trouvé"));

            // Statistiques générales
            long totalCandidats = candidatRepository.count();
            long totalCampagnes = campagneRepository.count();
            long totalVotes = voteRepository.count();
            long totalElecteurs = electeurRepository.count();

            // Candidat en tête (si résultats visibles)
            String candidatEnTete = candidatRepository.findAllOrderByVoteCountDesc()
                    .stream()
                    .findFirst()
                    .map(candidat -> candidat.getUsername())
                    .orElse("Aucun vote");

            return TableauBordElecteurDTO.builder()
                    .electeurAVote(electeur.isAVote())
                    .nombreCandidats(totalCandidats)
                    .nombreCampagnes(totalCampagnes)
                    .nombreTotalVotes(totalVotes)
                    .nombreTotalElecteurs(totalElecteurs)
                    .candidatEnTete(candidatEnTete)
                    .build();

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("💥 Erreur génération tableau bord: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la génération du tableau de bord", e);
        }
    }

    // ==================== DTOs SPÉCIFIQUES ====================

    /**
     * 👤 DTO pour profil électeur
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ElecteurProfilDTO {
        private ElecteurDTO electeur;
        private boolean aVote;
        private LocalDateTime dateInscription;
        private long nombreTotalElecteurs;
        private double tauxParticipationGlobal;
        private String messageStatut;
    }

    /**
     * 🏆 DTO pour candidat avec statut
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CandidatAvecStatutDTO {
        private CandidatDTO candidat;
        private long nombreVotes;
        private int nombreCampagnes;
    }

    /**
     * 📊 DTO pour résultats partiels
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ResultatsPartielsDTO {
        private long totalVotes;
        private long totalElecteurs;
        private double tauxParticipation;
        private List<ResultatCandidatDTO> resultatsParCandidat;
    }

    /**
     * 📊 DTO pour résultat candidat
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ResultatCandidatDTO {
        private String nomCandidat;
        private long nombreVotes;
        private double pourcentageVotes;
    }

    /**
     * 📊 DTO pour tableau de bord électeur
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TableauBordElecteurDTO {
        private boolean electeurAVote;
        private long nombreCandidats;
        private long nombreCampagnes;
        private long nombreTotalVotes;
        private long nombreTotalElecteurs;
        private String candidatEnTete;
    }
}