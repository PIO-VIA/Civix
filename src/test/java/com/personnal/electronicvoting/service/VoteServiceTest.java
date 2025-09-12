package com.personnal.electronicvoting.service;

import com.personnal.electronicvoting.mapper.CandidatMapper;
import com.personnal.electronicvoting.mapper.VoteMapper;
import com.personnal.electronicvoting.model.Candidat;
import com.personnal.electronicvoting.model.Electeur;
import com.personnal.electronicvoting.model.Vote;
import com.personnal.electronicvoting.repository.CandidatRepository;
import com.personnal.electronicvoting.repository.ElecteurRepository;
import com.personnal.electronicvoting.repository.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VoteServiceTest {

    @Mock
    private VoteRepository voteRepository;

    @Mock
    private ElecteurRepository electeurRepository;

    @Mock
    private CandidatRepository candidatRepository;

    @Mock
    private VoteMapper voteMapper;

    @Mock
    private CandidatMapper candidatMapper;

    @Mock
    private ElecteurService electeurService;

    @InjectMocks
    private VoteService voteService;

    private Electeur electeur;
    private Candidat candidat;

    @BeforeEach
    void setUp() {
        electeur = new Electeur();
        electeur.setExternalIdElecteur("electeur-uuid");
        electeur.setAVote(false);

        candidat = new Candidat();
        candidat.setExternalIdCandidat("candidat-uuid");
    }

    @Test
    void effectuerVote_shouldSaveVote_whenVoteIsValid() {
        when(electeurRepository.findByExternalIdElecteur(anyString())).thenReturn(Optional.of(electeur));
        when(candidatRepository.findByExternalIdCandidat(anyString())).thenReturn(Optional.of(candidat));
        when(voteRepository.existsByElecteur_externalIdElecteur(anyString())).thenReturn(false);
        when(voteRepository.save(any(Vote.class))).thenReturn(new Vote());

        var result = voteService.effectuerVote("electeur-uuid", "candidat-uuid");

        assertNotNull(result);
    }

    @Test
    void effectuerVote_shouldThrowException_whenElectorHasAlreadyVoted() {
        electeur.setAVote(true);
        when(electeurRepository.findByExternalIdElecteur(anyString())).thenReturn(Optional.of(electeur));
        when(candidatRepository.findByExternalIdCandidat(anyString())).thenReturn(Optional.of(candidat));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            voteService.effectuerVote("electeur-uuid", "candidat-uuid");
        });

        assertEquals("Vous avez déjà voté. Un seul vote par électeur est autorisé.", exception.getMessage());
    }

    @Test
    void electeurPeutVoter_shouldReturnTrue_whenElectorCanVote() {
        when(electeurRepository.findByExternalIdElecteur(anyString())).thenReturn(Optional.of(electeur));
        when(voteRepository.existsByElecteur_externalIdElecteur(anyString())).thenReturn(false);

        assertTrue(voteService.electeurPeutVoter("electeur-uuid"));
    }

    @Test
    void electeurPeutVoter_shouldReturnFalse_whenElectorCannotVote() {
        electeur.setAVote(true);
        when(electeurRepository.findByExternalIdElecteur(anyString())).thenReturn(Optional.of(electeur));

        assertFalse(voteService.electeurPeutVoter("electeur-uuid"));
    }

    @Test
    void electeurAVote_shouldReturnTrue_whenElectorHasVoted() {
        when(voteRepository.existsByElecteur_externalIdElecteur(anyString())).thenReturn(true);

        assertTrue(voteService.electeurAVote("electeur-uuid"));
    }

    @Test
    void electeurAVote_shouldReturnFalse_whenElectorHasNotVoted() {
        when(voteRepository.existsByElecteur_externalIdElecteur(anyString())).thenReturn(false);

        assertFalse(voteService.electeurAVote("electeur-uuid"));
    }

    @Test
    void obtenirResultatsVotes_shouldReturnVoteResults() {
        when(candidatRepository.findAll()).thenReturn(Collections.singletonList(candidat));
        when(voteRepository.count()).thenReturn(10L);
        when(candidatRepository.countVotesByCandidat(anyString())).thenReturn(5L);

        var result = voteService.obtenirResultatsVotes();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(50.0, result.get(0).getPourcentageVotes());
    }

    @Test
    void obtenirGagnant_shouldReturnWinner() {
        VoteService.ResultatVoteDTO winner = new VoteService.ResultatVoteDTO(null, 10L, 100.0, 1);
        when(candidatRepository.findAll()).thenReturn(Collections.singletonList(candidat));
        when(voteRepository.count()).thenReturn(10L);
        when(candidatRepository.countVotesByCandidat(anyString())).thenReturn(10L);

        var result = voteService.obtenirGagnant();

        assertNotNull(result);
        assertEquals(10L, result.getNombreVotes());
    }

    @Test
    void obtenirGagnant_shouldThrowException_whenNoResults() {
        when(candidatRepository.findAll()).thenReturn(Collections.emptyList());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            voteService.obtenirGagnant();
        });

        assertEquals("Aucun résultat disponible", exception.getMessage());
    }

    @Test
    void obtenirStatistiquesGenerales_shouldReturnGeneralStats() {
        when(electeurRepository.count()).thenReturn(20L);
        when(voteRepository.count()).thenReturn(10L);
        when(electeurRepository.findByaVoteTrue()).thenReturn(Collections.nCopies(10, new Electeur()));
        when(candidatRepository.count()).thenReturn(5L);
        when(candidatRepository.findAll()).thenReturn(Collections.emptyList());

        var result = voteService.obtenirStatistiquesGenerales();

        assertNotNull(result);
        assertEquals(50.0, result.getTauxParticipation());
    }

    @Test
    void obtenirRepartitionTemporelle_shouldReturnTemporalRepartition() {
        when(voteRepository.findAll()).thenReturn(Collections.emptyList());

        var result = voteService.obtenirRepartitionTemporelle();

        assertNotNull(result);
    }

    @Test
    void obtenirStatutVoteElecteur_shouldReturnStatus_whenElectorExists() {
        when(electeurRepository.findByExternalIdElecteur(anyString())).thenReturn(Optional.of(electeur));

        var result = voteService.obtenirStatutVoteElecteur("electeur-uuid");

        assertNotNull(result);
        assertFalse(result.isAVote());
    }

    @Test
    void obtenirStatutVoteElecteur_shouldThrowException_whenElectorDoesNotExist() {
        when(electeurRepository.findByExternalIdElecteur(anyString())).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            voteService.obtenirStatutVoteElecteur("electeur-uuid");
        });

        assertEquals("Électeur non trouvé", exception.getMessage());
    }
}
