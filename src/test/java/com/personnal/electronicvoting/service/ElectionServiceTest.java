package com.personnal.electronicvoting.service;

import com.personnal.electronicvoting.dto.request.VoterElectionRequest;
import com.personnal.electronicvoting.mapper.ElectionMapper;
import com.personnal.electronicvoting.mapper.VoteElectionMapper;
import com.personnal.electronicvoting.model.*;
import com.personnal.electronicvoting.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ElectionServiceTest {

    @Mock
    private ElectionRepository electionRepository;

    @Mock
    private VoteElectionRepository voteElectionRepository;

    @Mock
    private AdministrateurRepository administrateurRepository;

    @Mock
    private ElecteurRepository electeurRepository;

    @Mock
    private CandidatRepository candidatRepository;

    @Mock
    private ElectionMapper electionMapper;

    @Mock
    private VoteElectionMapper voteElectionMapper;

    @InjectMocks
    private ElectionService electionService;

    private Election election;
    private Electeur electeur;
    private Candidat candidat;

    @BeforeEach
    void setUp() {
        election = new Election();
        election.setExternalIdElection("election-uuid");
        election.setStatut(Election.StatutElection.EN_COURS);
        election.setDateDebut(LocalDate.now().minusDays(1));
        election.setDateFin(LocalDate.now().plusDays(1));
        election.setAutoriserVoteMultiple(false);

        electeur = new Electeur();
        electeur.setExternalIdElecteur("electeur-uuid");

        candidat = new Candidat();
        candidat.setExternalIdCandidat("candidat-uuid");

        Set<Candidat> participants = new HashSet<>();
        participants.add(candidat);
        election.setCandidats(participants);

        Set<Electeur> electeursAutorises = new HashSet<>();
        electeursAutorises.add(electeur);
        election.setElecteursAutorises(electeursAutorises);
    }

    @Test
    void listerToutesElections_shouldReturnAllElections() {
        when(electionRepository.findAll()).thenReturn(Collections.singletonList(election));

        var result = electionService.listerToutesElections();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void obtenirElection_shouldReturnElection_whenElectionExists() {
        when(electionRepository.findByExternalIdElection(anyString())).thenReturn(Optional.of(election));

        var result = electionService.obtenirElection("election-uuid");

        assertNotNull(result);
    }

    @Test
    void obtenirElection_shouldThrowException_whenElectionDoesNotExist() {
        when(electionRepository.findByExternalIdElection(anyString())).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            electionService.obtenirElection("election-uuid");
        });

        assertEquals("Élection non trouvée: election-uuid", exception.getMessage());
    }

    @Test
    void listerElectionsDisponiblesPourElecteur_shouldReturnAvailableElections() {
        when(electionRepository.findElectionsDisponiblesPourElecteur(anyString(), any(Election.StatutElection.class)))
                .thenReturn(Collections.singletonList(election));

        var result = electionService.listerElectionsDisponiblesPourElecteur("electeur-uuid");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void voterPourElection_shouldSaveVote_whenVoteIsValid() {
        VoterElectionRequest request = new VoterElectionRequest();
        request.setElectionId("election-uuid");
        request.setCandidatId("candidat-uuid");

        when(electionRepository.findByExternalIdElection(anyString())).thenReturn(Optional.of(election));
        when(electeurRepository.findByExternalIdElecteur(anyString())).thenReturn(Optional.of(electeur));
        when(candidatRepository.findByExternalIdCandidat(anyString())).thenReturn(Optional.of(candidat));
        when(voteElectionRepository.existsByElection_ExternalIdElectionAndElecteur_ExternalIdElecteur(anyString(), anyString())).thenReturn(false);
        when(voteElectionRepository.save(any(VoteElection.class))).thenReturn(new VoteElection());

        var result = electionService.voterPourElection(request, "electeur-uuid");

        assertNotNull(result);
    }

    @Test
    void voterPourElection_shouldThrowException_whenElectionDoesNotExist() {
        VoterElectionRequest request = new VoterElectionRequest();
        request.setElectionId("election-uuid");

        when(electionRepository.findByExternalIdElection(anyString())).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            electionService.voterPourElection(request, "electeur-uuid");
        });

        assertEquals("Élection non trouvée: election-uuid", exception.getMessage());
    }

    @Test
    void voterPourElection_shouldThrowException_whenElectorHasAlreadyVoted() {
        VoterElectionRequest request = new VoterElectionRequest();
        request.setElectionId("election-uuid");
        request.setCandidatId("candidat-uuid");

        when(electionRepository.findByExternalIdElection(anyString())).thenReturn(Optional.of(election));
        when(electeurRepository.findByExternalIdElecteur(anyString())).thenReturn(Optional.of(electeur));
        when(candidatRepository.findByExternalIdCandidat(anyString())).thenReturn(Optional.of(candidat));
        when(voteElectionRepository.existsByElection_ExternalIdElectionAndElecteur_ExternalIdElecteur(anyString(), anyString())).thenReturn(true);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            electionService.voterPourElection(request, "electeur-uuid");
        });

        assertEquals("L'électeur a déjà voté pour cette élection", exception.getMessage());
    }

    @Test
    void obtenirResultatsElection_shouldReturnResults_whenResultsAreAvailable() {
        election.setResultatsVisibles(true);
        when(electionRepository.findByExternalIdElection(anyString())).thenReturn(Optional.of(election));
        when(voteElectionRepository.countVotesParElection(anyString(), any(VoteElection.StatutVote.class))).thenReturn(10L);
        when(voteElectionRepository.countVotesParCandidatPourElection(anyString(), any(VoteElection.StatutVote.class)))
                .thenReturn(Collections.emptyList());

        var result = electionService.obtenirResultatsElection("election-uuid");

        assertNotNull(result);
        assertEquals(10L, result.getTotalVotes());
    }

    @Test
    void obtenirResultatsElection_shouldThrowException_whenElectionDoesNotExist() {
        when(electionRepository.findByExternalIdElection(anyString())).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            electionService.obtenirResultatsElection("election-uuid");
        });

        assertEquals("Élection non trouvée: election-uuid", exception.getMessage());
    }

    @Test
    void obtenirResultatsElection_shouldThrowException_whenResultsNotAvailable() {
        election.setResultatsVisibles(false);
        election.setStatut(Election.StatutElection.EN_COURS);
        when(electionRepository.findByExternalIdElection(anyString())).thenReturn(Optional.of(election));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            electionService.obtenirResultatsElection("election-uuid");
        });

        assertEquals("Les résultats ne sont pas encore disponibles", exception.getMessage());
    }
}
