package com.personnal.electronicvoting.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personnal.electronicvoting.service.CampagneService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CampagneController.class)
class CampagneControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CampagneService campagneService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listerToutesCampagnes_shouldReturnAllCampaigns() throws Exception {
        when(campagneService.listerToutesCampagnes()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/public/campagnes"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenirCampagne_shouldReturnCampaign_whenCampaignExists() throws Exception {
        when(campagneService.obtenirDetailCampagne("campagne-uuid")).thenReturn(new CampagneService.CampagneDetailDTO());

        mockMvc.perform(get("/api/public/campagnes/campagne-uuid"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenirCampagne_shouldReturnNotFound_whenCampaignDoesNotExist() throws Exception {
        when(campagneService.obtenirDetailCampagne("campagne-uuid")).thenThrow(new RuntimeException());

        mockMvc.perform(get("/api/public/campagnes/campagne-uuid"))
                .andExpect(status().isNotFound());
    }

    @Test
    void rechercherCampagnes_shouldReturnCampaigns_whenKeywordIsProvided() throws Exception {
        when(campagneService.rechercherCampagnesParMotCle("test")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/public/campagnes/recherche").param("motCle", "test"))
                .andExpect(status().isOk());
    }

    @Test
    void rechercherCampagnes_shouldReturnAllCampaigns_whenKeywordIsEmpty() throws Exception {
        when(campagneService.rechercherCampagnesParMotCle("")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/public/campagnes/recherche").param("motCle", ""))
                .andExpect(status().isOk());
    }

    @Test
    void obtenirCampagnesAvecPhotos_shouldReturnCampaignsWithPhotos() throws Exception {
        when(campagneService.obtenirCampagnesAvecPhotos()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/public/campagnes/avec-photos"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenirCampagnesAvecCandidats_shouldReturnCampaignsWithCandidates() throws Exception {
        when(campagneService.obtenirCampagnesAvecCandidats()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/public/campagnes/avec-candidats"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenirCampagnesParCandidat_shouldReturnCampaignsByCandidate() throws Exception {
        when(campagneService.obtenirCampagnesGroupeesParCandidat()).thenReturn(Collections.emptyMap());

        mockMvc.perform(get("/api/public/campagnes/par-candidat"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenirRepartition_shouldReturnRepartition() throws Exception {
        when(campagneService.obtenirRepartitionParCandidat()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/public/campagnes/repartition"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenirStatistiques_shouldReturnStatistics() throws Exception {
        when(campagneService.obtenirStatistiquesCampagnes()).thenReturn(new CampagneService.StatistiquesCampagnesDTO());

        mockMvc.perform(get("/api/public/campagnes/statistiques"))
                .andExpect(status().isOk());
    }

    @Test
    void filtrerParLongueurDescription_shouldReturnFilteredCampaigns() throws Exception {
        when(campagneService.listerToutesCampagnes()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/public/campagnes/filtre/par-longueur").param("minLength", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenirCampagnesCandidat_shouldReturnCampaigns_whenCandidateExists() throws Exception {
        when(campagneService.obtenirCampagnesParCandidat("candidat-uuid")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/public/campagnes/candidat/candidat-uuid"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenirCampagnesCandidat_shouldReturnNotFound_whenCandidateDoesNotExist() throws Exception {
        when(campagneService.obtenirCampagnesParCandidat("candidat-uuid")).thenThrow(new RuntimeException());

        mockMvc.perform(get("/api/public/campagnes/candidat/candidat-uuid"))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenirCampagnesTendance_shouldReturnTrendingCampaigns() throws Exception {
        when(campagneService.obtenirCampagnesAvecCandidats()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/public/campagnes/tendance").param("limite", "5"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenirCampagneAleatoire_shouldReturnRandomCampaign() throws Exception {
        when(campagneService.obtenirCampagnesAvecCandidats()).thenReturn(Collections.singletonList(new CampagneService.CampagneAvecCandidatDTO()));

        mockMvc.perform(get("/api/public/campagnes/aleatoire"))
                .andExpect(status().isOk());
    }

    @Test
    void exporterCampagnesSimple_shouldReturnSimpleExport() throws Exception {
        when(campagneService.obtenirCampagnesAvecCandidats()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/public/campagnes/export/simple"))
                .andExpect(status().isOk());
    }

    @Test
    void healthCheck_shouldReturnOk() throws Exception {
        when(campagneService.obtenirStatistiquesCampagnes()).thenReturn(new CampagneService.StatistiquesCampagnesDTO());

        mockMvc.perform(get("/api/public/campagnes/health"))
                .andExpect(status().isOk());
    }
}
