package com.personnal.electronicvoting.service;

import com.personnal.electronicvoting.dto.ElectionDTO;
import com.personnal.electronicvoting.dto.VoteElectionDTO;
import com.personnal.electronicvoting.dto.request.CreateElectionRequest;
import com.personnal.electronicvoting.dto.request.UpdateElectionRequest;
import com.personnal.electronicvoting.dto.request.VoterElectionRequest;
import com.personnal.electronicvoting.mapper.ElectionMapper;
import com.personnal.electronicvoting.mapper.VoteElectionMapper;
import com.personnal.electronicvoting.model.*;
import com.personnal.electronicvoting.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ElectionService {

    private final ElectionRepository electionRepository;
    private final VoteElectionRepository voteElectionRepository;
    private final AdministrateurRepository administrateurRepository;
    private final ElecteurRepository electeurRepository;
    private final CandidatRepository candidatRepository;
    private final ElectionMapper electionMapper;
    private final VoteElectionMapper voteElectionMapper;

    // ==================== GESTION ÉLECTIONS ADMINISTRATEUR ====================







    // ==================== CONSULTATION ÉLECTIONS ====================

    public List<ElectionDTO> listerToutesElections() {
        log.info("📋 Consultation de toutes les élections");

        List<Election> elections = electionRepository.findAll();
        log.info("📊 {} élections trouvées", elections.size());

        return elections.stream()
                .map(electionMapper::toDTO)
                .toList();
    }

    public ElectionDTO obtenirElection(String electionId) {
        log.info("🔍 Consultation de l'élection: {}", electionId);

        Election election = electionRepository.findByExternalIdElection(electionId)
                .orElseThrow(() -> new RuntimeException("Élection non trouvée: " + electionId));

        return electionMapper.toDTO(election);
    }



    public List<ElectionDTO> listerElectionsDisponiblesPourElecteur(String electeurId) {
        log.info("🗳️ Consultation des élections disponibles pour l'électeur: {}", electeurId);

        List<Election> elections = electionRepository.findElectionsDisponiblesPourElecteur(
                electeurId, Election.StatutElection.EN_COURS);

        List<Election> electionsFiltrees = elections.stream()
                .filter(Election::estActive)
                .toList();

        log.info("📊 {} élections disponibles pour l'électeur", electionsFiltrees.size());

        return electionsFiltrees.stream()
                .map(electionMapper::toDTO)
                .toList();
    }

    // ==================== GESTION DES VOTES ====================

    @Transactional
    public VoteElectionDTO voterPourElection(VoterElectionRequest request, String electeurId) {
        log.info("🗳️ Vote de l'électeur {} pour l'élection {}", electeurId, request.getElectionId());

        Election election = electionRepository.findByExternalIdElection(request.getElectionId())
                .orElseThrow(() -> new RuntimeException("Élection non trouvée: " + request.getElectionId()));

        Electeur electeur = electeurRepository.findByExternalIdElecteur(electeurId)
                .orElseThrow(() -> new RuntimeException("Électeur non trouvé: " + electeurId));

        Candidat candidat = candidatRepository.findByExternalIdCandidat(request.getCandidatId())
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé: " + request.getCandidatId()));

        validateVote(election, electeur, candidat);

        if (!election.getAutoriserVoteMultiple()) {
            boolean aDejaVote = voteElectionRepository.existsByElection_ExternalIdElectionAndElecteur_ExternalIdElecteur(
                    request.getElectionId(), electeurId);
            if (aDejaVote) {
                throw new RuntimeException("L'électeur a déjà voté pour cette élection");
            }
        } else {
            Long nombreVotesExistants = voteElectionRepository.countVotesElecteurPourElection(
                    request.getElectionId(), electeurId, VoteElection.StatutVote.VALIDE);
            if (nombreVotesExistants >= election.getNombreMaxVotesParElecteur()) {
                throw new RuntimeException("Nombre maximum de votes atteint pour cette élection");
            }
        }

        VoteElection vote = VoteElection.builder()
                .election(election)
                .electeur(electeur)
                .candidat(candidat)
                .adresseIp(request.getAdresseIp())
                .userAgent(request.getUserAgent())
                .statutVote(VoteElection.StatutVote.VALIDE)
                .build();

        VoteElection voteSauvegarde = voteElectionRepository.save(vote);
        log.info("✅ Vote enregistré avec l'ID: {}", voteSauvegarde.getId());

        return voteElectionMapper.toDTO(voteSauvegarde);
    }

    // ==================== RÉSULTATS ET STATISTIQUES ====================

    public ResultatsElectionDTO obtenirResultatsElection(String electionId) {
        log.info("📊 Consultation des résultats de l'élection: {}", electionId);

        Election election = electionRepository.findByExternalIdElection(electionId)
                .orElseThrow(() -> new RuntimeException("Élection non trouvée: " + electionId));

        if (!election.getResultatsVisibles() && election.getStatut() != Election.StatutElection.TERMINEE) {
            throw new RuntimeException("Les résultats ne sont pas encore disponibles");
        }

        Long totalVotes = voteElectionRepository.countVotesParElection(electionId, VoteElection.StatutVote.VALIDE);

        List<Object[]> resultatsParCandidat = voteElectionRepository.countVotesParCandidatPourElection(
                electionId, VoteElection.StatutVote.VALIDE);

        List<ResultatCandidatDTO> resultats = resultatsParCandidat.stream()
                .map(resultat -> {
                    String candidatId = (String) resultat[0];
                    Long nombreVotes = (Long) resultat[1];
                    
                    Candidat candidat = candidatRepository.findByExternalIdCandidat(candidatId)
                            .orElse(null);
                    
                    double pourcentage = totalVotes > 0 ? (double) nombreVotes / totalVotes * 100 : 0;
                    
                    return ResultatCandidatDTO.builder()
                            .candidatId(candidatId)
                            .candidatNom(candidat != null ? candidat.getUsername() : "Candidat inconnu")
                            .nombreVotes(nombreVotes)
                            .pourcentageVotes(Math.round(pourcentage * 100.0) / 100.0)
                            .build();
                })
                .sorted((a, b) -> Long.compare(b.getNombreVotes(), a.getNombreVotes()))
                .toList();

        return ResultatsElectionDTO.builder()
                .electionId(electionId)
                .electionTitre(election.getTitre())
                .totalVotes(totalVotes)
                .totalElecteursAutorises((long) election.getElecteursAutorises().size())
                .tauxParticipation(election.getElecteursAutorises().size() > 0 ? 
                        (double) totalVotes / election.getElecteursAutorises().size() * 100 : 0)
                .resultatsParCandidat(resultats)
                .dateCalcul(LocalDate.now())
                .build();
    }

    // ==================== VALIDATION ====================



    private void validateVote(Election election, Electeur electeur, Candidat candidat) {
        if (!election.estActive()) {
            throw new RuntimeException("Cette élection n'est pas active");
        }



        if (!election.electeurEstAutorise(electeur.getExternalIdElecteur())) {
            throw new RuntimeException("Électeur non autorisé pour cette élection");
        }

        if (!election.candidatEstParticipant(candidat.getExternalIdCandidat())) {
            throw new RuntimeException("Candidat non participant à cette élection");
        }
    }

    // ==================== DTOs SPÉCIFIQUES ====================

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ResultatsElectionDTO {
        private String electionId;
        private String electionTitre;
        private Long totalVotes;
        private Long totalElecteursAutorises;
        private Double tauxParticipation;
        private List<ResultatCandidatDTO> resultatsParCandidat;
        private LocalDate dateCalcul;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ResultatCandidatDTO {
        private String candidatId;
        private String candidatNom;
        private Long nombreVotes;
        private Double pourcentageVotes;
    }
}