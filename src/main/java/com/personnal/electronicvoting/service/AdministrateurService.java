package com.personnal.electronicvoting.service;

import com.personnal.electronicvoting.dto.ElecteurDTO;
import com.personnal.electronicvoting.dto.CandidatDTO;
import com.personnal.electronicvoting.dto.CampagneDTO;
import com.personnal.electronicvoting.dto.request.*;
import com.personnal.electronicvoting.model.*;
import com.personnal.electronicvoting.repository.*;
import com.personnal.electronicvoting.mapper.*;
import com.personnal.electronicvoting.util.PasswordGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdministrateurService {

    private final ElecteurRepository electeurRepository;
    private final CandidatRepository candidatRepository;
    private final CampagneRepository campagneRepository;
    private final AdministrateurRepository administrateurRepository;
    private final VoteRepository voteRepository;

    private final UserMapper userMapper;
    private final CandidatMapper candidatMapper;
    private final CampagneMapper campagneMapper;
    private final AdminMapper adminMapper;

    private final PasswordEncoder passwordEncoder;
    private final PasswordGenerator passwordGenerator;
    private final EmailService emailService;

    // ==================== GESTION ÉLECTEURS ====================

    /**
     *  Créer un électeur avec envoi automatique des identifiants
     */
    public ElecteurDTO creerElecteur(CreateElecteurAdminRequest request) {
        log.info(" Création électeur par admin - Username: {}, Email: {}",
                request.getUsername(), request.getEmail());

        try {
            //  Vérifications uniques
            if (electeurRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Un électeur avec cet email existe déjà");
            }

            //  Génération mot de passe temporaire
            String motDePasseTemporaire = passwordGenerator.genererMotDePasseTemporaire();

            //  Création électeur
            Electeur electeur = new Electeur();
            electeur.setUsername(request.getUsername());
            electeur.setEmail(request.getEmail());
            electeur.setMotDePasse(passwordEncoder.encode(motDePasseTemporaire));
            electeur.setEmpreinteDigitale(request.getEmpreinteDigitale());
            electeur.setAVote(false);

            Electeur electeurSauve = electeurRepository.save(electeur);

            //  Envoi des identifiants par email
            emailService.envoyerIdentifiantsElecteur(
                    request.getEmail(),
                    request.getUsername(),
                    motDePasseTemporaire
            );

            log.info(" Électeur créé avec succès - ID: {}", electeurSauve.getExternalIdElecteur());
            return userMapper.toDTO(electeurSauve);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error(" Erreur création électeur: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la création de l'électeur", e);
        }
    }

    /**
     *  Lister tous les électeurs
     */
    @Transactional(readOnly = true)
    public List<ElecteurDTO> listerElecteurs() {
        log.info(" Admin - Liste de tous les électeurs");
        return electeurRepository.findAll()
                .stream()
                .map(userMapper::toDTO)
                .toList();
    }

    /**
     *  Trouver électeur par ID
     */
    @Transactional(readOnly = true)
    public ElecteurDTO trouverElecteur(String externalId) {
        log.info(" Admin - Recherche électeur: {}", externalId);
        return electeurRepository.findByExternalIdElecteur(externalId)
                .map(userMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Électeur non trouvé: " + externalId));
    }

    /**
     * Modifier un électeur
     */
    public ElecteurDTO modifierElecteur(String externalId, UpdateElecteurRequest request) {
        log.info(" Admin - Modification électeur: {}", externalId);

        try {
            Electeur electeur = electeurRepository.findByExternalIdElecteur(externalId)
                    .orElseThrow(() -> new RuntimeException("Électeur non trouvé"));

            //  Mise à jour des champs (si fournis)
            if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
                electeur.setUsername(request.getUsername().trim());
            }

            if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                // Vérifier unicité email
                if (!request.getEmail().equals(electeur.getEmail()) &&
                        electeurRepository.existsByEmail(request.getEmail())) {
                    throw new RuntimeException("Cet email est déjà utilisé");
                }
                electeur.setEmail(request.getEmail().trim());
            }

            //  Reset mot de passe si demandé
            if (request.isResetMotDePasse()) {
                String nouveauMotDePasse = passwordGenerator.genererMotDePasseTemporaire();
                electeur.setMotDePasse(passwordEncoder.encode(nouveauMotDePasse));

                // Envoyer nouveau mot de passe par email
                emailService.envoyerNouveauMotDePasse(
                        electeur.getEmail(),
                        electeur.getUsername(),
                        nouveauMotDePasse
                );
                log.info(" Mot de passe réinitialisé pour électeur: {}", externalId);
            }

            Electeur electeurMisAJour = electeurRepository.save(electeur);
            log.info(" Électeur modifié avec succès: {}", externalId);

            return userMapper.toDTO(electeurMisAJour);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error(" Erreur modification électeur: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la modification", e);
        }
    }

    /**
     *  Supprimer un électeur
     */
    public void supprimerElecteur(String externalId) {
        log.info(" Admin - Suppression électeur: {}", externalId);

        try {
            Electeur electeur = electeurRepository.findByExternalIdElecteur(externalId)
                    .orElseThrow(() -> new RuntimeException("Électeur non trouvé"));

            // ️ Vérifier si l'électeur a voté (décision métier)
            if (electeur.isAVote()) {
                log.warn("️ Tentative suppression électeur ayant voté: {}", externalId);
                throw new RuntimeException("Impossible de supprimer un électeur qui a voté");
            }

            electeurRepository.delete(electeur);
            log.info(" Électeur supprimé avec succès: {}", externalId);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error(" Erreur suppression électeur: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la suppression", e);
        }
    }

    // ==================== GESTION CANDIDATS ====================

    /**
     *  Créer un candidat
     */
    public CandidatDTO creerCandidat(CreateCandidatRequest request) {
        log.info(" Création candidat par admin - Username: {}", request.getUsername());

        try {
            if (candidatRepository.existsByUsername(request.getUsername())) {
                throw new RuntimeException("Un candidat avec ce nom existe déjà");
            }

            Candidat candidat = new Candidat();
            candidat.setUsername(request.getUsername());
            candidat.setDescription(request.getDescription());
            candidat.setPhoto(request.getPhoto());

            Candidat candidatSauve = candidatRepository.save(candidat);
            log.info(" Candidat créé avec succès - ID: {}", candidatSauve.getExternalIdCandidat());

            return candidatMapper.toDTO(candidatSauve);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error(" Erreur création candidat: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la création du candidat", e);
        }
    }

    /**
     *  Lister tous les candidats
     */
    @Transactional(readOnly = true)
    public List<CandidatDTO> listerCandidats() {
        log.info(" Admin - Liste de tous les candidats");
        return candidatRepository.findAll()
                .stream()
                .map(candidatMapper::toDTO)
                .toList();
    }

    /**
     *  Modifier un candidat
     */
    public CandidatDTO modifierCandidat(String externalId, UpdateCandidatRequest request) {
        log.info(" Admin - Modification candidat: {}", externalId);

        try {
            Candidat candidat = candidatRepository.findByExternalIdCandidat(externalId)
                    .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

            if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
                // Vérifier unicité username
                if (!request.getUsername().equals(candidat.getUsername()) &&
                        candidatRepository.existsByUsername(request.getUsername())) {
                    throw new RuntimeException("Ce nom de candidat est déjà utilisé");
                }
                candidat.setUsername(request.getUsername().trim());
            }

            if (request.getDescription() != null) {
                candidat.setDescription(request.getDescription().trim());
            }

            if (request.getPhoto() != null) {
                candidat.setPhoto(request.getPhoto().trim());
            }

            Candidat candidatMisAJour = candidatRepository.save(candidat);
            log.info(" Candidat modifié avec succès: {}", externalId);

            return candidatMapper.toDTO(candidatMisAJour);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error(" Erreur modification candidat: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la modification", e);
        }
    }

    /**
     * ️ Supprimer un candidat
     */
    public void supprimerCandidat(String externalId) {
        log.info(" Admin - Suppression candidat: {}", externalId);

        try {
            Candidat candidat = candidatRepository.findByExternalIdCandidat(externalId)
                    .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

            // ️ Vérifier s'il a des votes (décision métier)
            long nombreVotes = candidatRepository.countVotesByCandidat(externalId);
            if (nombreVotes > 0) {
                log.warn("️ Tentative suppression candidat avec votes: {} ({} votes)",
                        externalId, nombreVotes);
                throw new RuntimeException("Impossible de supprimer un candidat qui a des votes");
            }

            candidatRepository.delete(candidat);
            log.info(" Candidat supprimé avec succès: {}", externalId);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error(" Erreur suppression candidat: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la suppression", e);
        }
    }

    // ==================== GESTION CAMPAGNES ====================

    /**
     *  Créer une campagne pour un candidat
     */
    public CampagneDTO creerCampagne(CreateCampagneRequest request) {
        log.info(" Création campagne par admin - Candidat: {}", request.getCandidatId());

        try {
            Candidat candidat = candidatRepository.findByExternalIdCandidat(request.getCandidatId())
                    .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

            Campagne campagne = new Campagne();
            campagne.setDescription(request.getDescription());
            campagne.setPhoto(request.getPhoto());
            campagne.setCandidat(candidat);

            Campagne campagneSauvee = campagneRepository.save(campagne);
            log.info(" Campagne créée avec succès - ID: {}", campagneSauvee.getExternalIdCampagne());

            return campagneMapper.toDTO(campagneSauvee);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error(" Erreur création campagne: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la création de la campagne", e);
        }
    }

    /**
     *  Lister toutes les campagnes
     */
    @Transactional(readOnly = true)
    public List<CampagneDTO> listerCampagnes() {
        log.info(" Admin - Liste de toutes les campagnes");
        return campagneRepository.findAll()
                .stream()
                .map(campagneMapper::toDTO)
                .toList();
    }

    /**
     * ️ Modifier une campagne
     */
    public CampagneDTO modifierCampagne(String externalId, UpdateCampagneRequest request) {
        log.info("✏ Admin - Modification campagne: {}", externalId);

        try {
            Campagne campagne = campagneRepository.findByExternalIdCampagne(externalId)
                    .orElseThrow(() -> new RuntimeException("Campagne non trouvée"));

            if (request.getDescription() != null && !request.getDescription().trim().isEmpty()) {
                campagne.setDescription(request.getDescription().trim());
            }

            if (request.getPhoto() != null) {
                campagne.setPhoto(request.getPhoto().trim());
            }

            Campagne campagneMiseAJour = campagneRepository.save(campagne);
            log.info(" Campagne modifiée avec succès: {}", externalId);

            return campagneMapper.toDTO(campagneMiseAJour);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error(" Erreur modification campagne: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la modification", e);
        }
    }

    /**
     *  Supprimer une campagne
     */
    public void supprimerCampagne(String externalId) {
        log.info(" Admin - Suppression campagne: {}", externalId);

        try {
            Campagne campagne = campagneRepository.findByExternalIdCampagne(externalId)
                    .orElseThrow(() -> new RuntimeException("Campagne non trouvée"));

            campagneRepository.delete(campagne);
            log.info(" Campagne supprimée avec succès: {}", externalId);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error(" Erreur suppression campagne: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la suppression", e);
        }
    }

    // ==================== STATISTIQUES ====================

    /**
     *  Obtenir statistiques générales (pour tableau de bord admin)
     */
    @Transactional(readOnly = true)
    public StatistiquesAdminDTO obtenirStatistiques() {
        log.info(" Admin - Calcul statistiques générales");

        long totalElecteurs = electeurRepository.count();
        long electeursAyantVote = electeurRepository.findByaVoteTrue().size();
        long totalCandidats = candidatRepository.count();
        long totalCampagnes = campagneRepository.count();
        long totalVotes = voteRepository.count();

        double tauxParticipation = totalElecteurs > 0 ?
                (double) electeursAyantVote / totalElecteurs * 100 : 0;

        return StatistiquesAdminDTO.builder()
                .totalElecteurs(totalElecteurs)
                .electeursAyantVote(electeursAyantVote)
                .totalCandidats(totalCandidats)
                .totalCampagnes(totalCampagnes)
                .totalVotes(totalVotes)
                .tauxParticipation(Math.round(tauxParticipation * 100.0) / 100.0)
                .build();
    }

    /**
     *  DTO pour les statistiques admin
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class StatistiquesAdminDTO {
        private long totalElecteurs;
        private long electeursAyantVote;
        private long totalCandidats;
        private long totalCampagnes;
        private long totalVotes;
        private double tauxParticipation;
    }
}