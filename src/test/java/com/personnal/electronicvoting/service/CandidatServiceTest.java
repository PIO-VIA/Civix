package com.personnal.electronicvoting.service;

import com.personnal.electronicvoting.mapper.CampagneMapper;
import com.personnal.electronicvoting.mapper.CandidatMapper;
import com.personnal.electronicvoting.model.Candidat;
import com.personnal.electronicvoting.repository.CampagneRepository;
import com.personnal.electronicvoting.repository.CandidatRepository;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CandidatServiceTest {

    @Mock
    private CandidatRepository candidatRepository;

    @Mock
    private CampagneRepository campagneRepository;

    @Mock
    private VoteRepository voteRepository;

    @Mock
    private CandidatMapper candidatMapper;

    @Mock
    private CampagneMapper campagneMapper;

    @InjectMocks
    private CandidatService candidatService;

    private Candidat candidat;

    @BeforeEach
    void setUp() {
        candidat = new Candidat();
        candidat.setExternalIdCandidat("test-uuid");
    }

    @Test
    void listerTousCandidats_shouldReturnAllCandidats() {
        when(candidatRepository.findAll()).thenReturn(Collections.singletonList(candidat));

        var result = candidatService.listerTousCandidats();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void obtenirClassementCandidats_shouldReturnRankedCandidats() {
        when(candidatRepository.findAllOrderByVoteCountDesc()).thenReturn(Collections.singletonList(candidat));
        when(candidatRepository.countVotesByCandidat(anyString())).thenReturn(10L);

        var result = candidatService.obtenirClassementCandidats();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getNombreVotes());
    }

    @Test
    void trouverCandidatParId_shouldReturnCandidat_whenCandidatExists() {
        when(candidatRepository.findByExternalIdCandidat(anyString())).thenReturn(Optional.of(candidat));

        var result = candidatService.trouverCandidatParId("test-uuid");

        assertNotNull(result);
    }

    @Test
    void trouverCandidatParId_shouldThrowException_whenCandidatDoesNotExist() {
        when(candidatRepository.findByExternalIdCandidat(anyString())).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            candidatService.trouverCandidatParId("test-uuid");
        });

        assertEquals("Candidat non trouvé: test-uuid", exception.getMessage());
    }

    @Test
    void rechercherCandidatsParNom_shouldReturnMatchingCandidats_whenNameIsProvided() {
        when(candidatRepository.findByUsernameContaining(anyString())).thenReturn(Collections.singletonList(candidat));

        var result = candidatService.rechercherCandidatsParNom("test");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void rechercherCandidatsParNom_shouldReturnAllCandidats_whenNameIsNullOrEmpty() {
        when(candidatRepository.findAll()).thenReturn(Collections.singletonList(candidat));

        var result = candidatService.rechercherCandidatsParNom(null);
        assertNotNull(result);
        assertEquals(1, result.size());

        result = candidatService.rechercherCandidatsParNom("  ");
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void obtenirCampagnesCandidat_shouldReturnCampaigns_whenCandidatExists() {
        when(candidatRepository.findByExternalIdCandidat(anyString())).thenReturn(Optional.of(candidat));
        when(campagneRepository.findByCandidat_ExternalIdCandidat(anyString())).thenReturn(Collections.emptyList());

        var result = candidatService.obtenirCampagnesCandidat("test-uuid");

        assertNotNull(result);
    }

    @Test
    void obtenirCampagnesCandidat_shouldThrowException_whenCandidatDoesNotExist() {
        when(candidatRepository.findByExternalIdCandidat(anyString())).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            candidatService.obtenirCampagnesCandidat("test-uuid");
        });

        assertEquals("Candidat non trouvé: test-uuid", exception.getMessage());
    }

    @Test
    void obtenirDetailCandidat_shouldReturnDetails_whenCandidatExists() {
        when(candidatRepository.findByExternalIdCandidat(anyString())).thenReturn(Optional.of(candidat));
        when(campagneRepository.findByCandidat_ExternalIdCandidat(anyString())).thenReturn(Collections.emptyList());
        when(candidatRepository.countVotesByCandidat(anyString())).thenReturn(5L);

        var result = candidatService.obtenirDetailCandidat("test-uuid");

        assertNotNull(result);
        assertEquals(5L, result.getNombreVotes());
    }

    @Test
    void obtenirDetailCandidat_shouldThrowException_whenCandidatDoesNotExist() {
        when(candidatRepository.findByExternalIdCandidat(anyString())).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            candidatService.obtenirDetailCandidat("test-uuid");
        });

        assertEquals("Candidat non trouvé: test-uuid", exception.getMessage());
    }

    @Test
    void obtenirCandidatEnTete_shouldReturnLeadingCandidat() {
        CandidatService.CandidatAvecVotesDTO leadingCandidat = new CandidatService.CandidatAvecVotesDTO(null, 10L);
        when(candidatRepository.findAllOrderByVoteCountDesc()).thenReturn(Collections.singletonList(candidat));
        when(candidatRepository.countVotesByCandidat(anyString())).thenReturn(10L);

        var result = candidatService.obtenirCandidatEnTete();

        assertNotNull(result);
        assertEquals(10L, result.getNombreVotes());
    }

    @Test
    void obtenirCandidatEnTete_shouldThrowException_whenNoCandidates() {
        when(candidatRepository.findAllOrderByVoteCountDesc()).thenReturn(Collections.emptyList());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            candidatService.obtenirCandidatEnTete();
        });

        assertEquals("Aucun candidat trouvé", exception.getMessage());
    }

    @Test
    void peutEtreSupprime_shouldReturnTrue_whenNoVotes() {
        when(candidatRepository.countVotesByCandidat(anyString())).thenReturn(0L);

        assertTrue(candidatService.peutEtreSupprime("test-uuid"));
    }

    @Test
    void peutEtreSupprime_shouldReturnFalse_whenHasVotes() {
        when(candidatRepository.countVotesByCandidat(anyString())).thenReturn(5L);

        assertFalse(candidatService.peutEtreSupprime("test-uuid"));
    }

    @Test
    void obtenirStatistiquesDetaillees_shouldReturnDetailedStats() {
        when(candidatRepository.findAll()).thenReturn(Collections.singletonList(candidat));
        when(voteRepository.count()).thenReturn(10L);
        when(candidatRepository.countVotesByCandidat(anyString())).thenReturn(5L);
        when(campagneRepository.findByCandidat_ExternalIdCandidat(anyString())).thenReturn(Collections.emptyList());

        var result = candidatService.obtenirStatistiquesDetaillees();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(50.0, result.get(0).getPourcentageVotes());
        assertEquals(1, result.get(0).getRang());
    }
}
