package com.personnal.electronicvoting.service;

import com.personnal.electronicvoting.mapper.CampagneMapper;
import com.personnal.electronicvoting.mapper.CandidatMapper;
import com.personnal.electronicvoting.model.Campagne;
import com.personnal.electronicvoting.model.Candidat;
import com.personnal.electronicvoting.repository.CampagneRepository;
import com.personnal.electronicvoting.repository.CandidatRepository;
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
class CampagneServiceTest {

    @Mock
    private CampagneRepository campagneRepository;

    @Mock
    private CandidatRepository candidatRepository;

    @Mock
    private CampagneMapper campagneMapper;

    @Mock
    private CandidatMapper candidatMapper;

    @InjectMocks
    private CampagneService campagneService;

    private Campagne campagne;
    private Candidat candidat;

    @BeforeEach
    void setUp() {
        candidat = new Candidat();
        candidat.setExternalIdCandidat("candidat-uuid");

        campagne = new Campagne();
        campagne.setExternalIdCampagne("campagne-uuid");
        campagne.setCandidat(candidat);
        campagne.setDescription("description");
    }

    @Test
    void listerToutesCampagnes_shouldReturnAllCampaigns() {
        when(campagneRepository.findAll()).thenReturn(Collections.singletonList(campagne));

        var result = campagneService.listerToutesCampagnes();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void trouverCampagneParId_shouldReturnCampaign_whenCampaignExists() {
        when(campagneRepository.findByExternalIdCampagne(anyString())).thenReturn(Optional.of(campagne));

        var result = campagneService.trouverCampagneParId("campagne-uuid");

        assertNotNull(result);
    }

    @Test
    void trouverCampagneParId_shouldThrowException_whenCampaignDoesNotExist() {
        when(campagneRepository.findByExternalIdCampagne(anyString())).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            campagneService.trouverCampagneParId("campagne-uuid");
        });

        assertEquals("Campagne non trouvée: campagne-uuid", exception.getMessage());
    }

    @Test
    void obtenirCampagnesParCandidat_shouldReturnCampaigns_whenCandidatExists() {
        when(candidatRepository.findByExternalIdCandidat(anyString())).thenReturn(Optional.of(candidat));
        when(campagneRepository.findByCandidat_ExternalIdCandidat(anyString())).thenReturn(Collections.singletonList(campagne));

        var result = campagneService.obtenirCampagnesParCandidat("candidat-uuid");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void obtenirCampagnesParCandidat_shouldThrowException_whenCandidatDoesNotExist() {
        when(candidatRepository.findByExternalIdCandidat(anyString())).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            campagneService.obtenirCampagnesParCandidat("candidat-uuid");
        });

        assertEquals("Candidat non trouvé: candidat-uuid", exception.getMessage());
    }

    @Test
    void obtenirCampagnesAvecCandidats_shouldReturnCampaignsWithCandidates() {
        when(campagneRepository.findAll()).thenReturn(Collections.singletonList(campagne));

        var result = campagneService.obtenirCampagnesAvecCandidats();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void obtenirCampagnesGroupeesParCandidat_shouldReturnGroupedCampaigns() {
        when(campagneRepository.findAll()).thenReturn(Collections.singletonList(campagne));

        var result = campagneService.obtenirCampagnesGroupeesParCandidat();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("candidat-uuid"));
    }

    @Test
    void obtenirDetailCampagne_shouldReturnDetails_whenCampaignExists() {
        when(campagneRepository.findByExternalIdCampagne(anyString())).thenReturn(Optional.of(campagne));
        when(campagneRepository.findByCandidat_ExternalIdCandidat(anyString())).thenReturn(Collections.singletonList(campagne));

        var result = campagneService.obtenirDetailCampagne("campagne-uuid");

        assertNotNull(result);
        assertEquals(1, result.getNombreCampagnesCandidat());
    }

    @Test
    void obtenirDetailCampagne_shouldThrowException_whenCampaignDoesNotExist() {
        when(campagneRepository.findByExternalIdCampagne(anyString())).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            campagneService.obtenirDetailCampagne("campagne-uuid");
        });

        assertEquals("Campagne non trouvée: campagne-uuid", exception.getMessage());
    }

    @Test
    void rechercherCampagnesParMotCle_shouldReturnMatchingCampaigns_whenKeywordIsProvided() {
        when(campagneRepository.findAll()).thenReturn(Collections.singletonList(campagne));

        var result = campagneService.rechercherCampagnesParMotCle("desc");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void rechercherCampagnesParMotCle_shouldReturnAllCampaigns_whenKeywordIsNullOrEmpty() {
        when(campagneRepository.findAll()).thenReturn(Collections.singletonList(campagne));

        var result = campagneService.rechercherCampagnesParMotCle(null);
        assertNotNull(result);
        assertEquals(1, result.size());

        result = campagneService.rechercherCampagnesParMotCle("  ");
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void obtenirCampagnesAvecPhotos_shouldReturnCampaignsWithPhotos() {
        campagne.setPhoto("photo-url");
        when(campagneRepository.findAll()).thenReturn(Collections.singletonList(campagne));

        var result = campagneService.obtenirCampagnesAvecPhotos();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void obtenirStatistiquesCampagnes_shouldReturnCampaignStats() {
        when(campagneRepository.findAll()).thenReturn(Collections.singletonList(campagne));

        var result = campagneService.obtenirStatistiquesCampagnes();

        assertNotNull(result);
        assertEquals(1, result.getTotalCampagnes());
    }

    @Test
    void obtenirRepartitionParCandidat_shouldReturnRepartition() {
        when(campagneRepository.findAll()).thenReturn(Collections.singletonList(campagne));
        when(candidatRepository.findByExternalIdCandidat(anyString())).thenReturn(Optional.of(candidat));

        var result = campagneService.obtenirRepartitionParCandidat();

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
