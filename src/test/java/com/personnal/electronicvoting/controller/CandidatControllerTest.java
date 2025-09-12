package com.personnal.electronicvoting.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personnal.electronicvoting.service.CampagneService;
import com.personnal.electronicvoting.service.CandidatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CandidatController.class)
class CandidatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CandidatService candidatService;

    @MockBean
    private CampagneService campagneService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listerTousCandidats_shouldReturnAllCandidats() throws Exception {
        when(candidatService.listerTousCandidats()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/public/candidats"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenirCandidat_shouldReturnCandidat_whenCandidatExists() throws Exception {
        when(candidatService.obtenirDetailCandidat("candidat-uuid")).thenReturn(new CandidatService.CandidatDetailDTO());

        mockMvc.perform(get("/api/public/candidats/candidat-uuid"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenirCandidat_shouldReturnNotFound_whenCandidatDoesNotExist() throws Exception {
        when(candidatService.obtenirDetailCandidat("candidat-uuid")).thenThrow(new RuntimeException());

        mockMvc.perform(get("/api/public/candidats/candidat-uuid"))
                .andExpect(status().isNotFound());
    }

    @Test
    void rechercherCandidats_shouldReturnCandidats_whenNameIsProvided() throws Exception {
        when(candidatService.rechercherCandidatsParNom("test")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/public/candidats/recherche").param("nom", "test"))
                .andExpect(status().isOk());
    }

    @Test
    void rechercherCandidats_shouldReturnAllCandidats_whenNameIsEmpty() throws Exception {
        when(candidatService.rechercherCandidatsParNom("")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/public/candidats/recherche").param("nom", ""))
                .andExpect(status().isOk());
    }

    @Test
    void obtenirClassement_shouldReturnRanking() throws Exception {
        when(candidatService.obtenirClassementCandidats()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/public/candidats/classement"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenirCandidatEnTete_shouldReturnLeadingCandidat() throws Exception {
        when(candidatService.obtenirCandidatEnTete()).thenReturn(new CandidatService.CandidatAvecVotesDTO());

        mockMvc.perform(get("/api/public/candidats/en-tete"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenirCandidatEnTete_shouldReturnNotFound_whenNoCandidates() throws Exception {
        when(candidatService.obtenirCandidatEnTete()).thenThrow(new RuntimeException());

        mockMvc.perform(get("/api/public/candidats/en-tete"))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenirStatistiques_shouldReturnStatistics() throws Exception {
        when(candidatService.obtenirStatistiquesDetaillees()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/public/candidats/statistiques"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenirCampagnesCandidat_shouldReturnCampaigns_whenCandidatExists() throws Exception {
        when(candidatService.obtenirCampagnesCandidat("candidat-uuid")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/public/candidats/candidat-uuid/campagnes"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenirCampagnesCandidat_shouldReturnNotFound_whenCandidatDoesNotExist() throws Exception {
        when(candidatService.obtenirCampagnesCandidat("candidat-uuid")).thenThrow(new RuntimeException());

        mockMvc.perform(get("/api/public/candidats/candidat-uuid/campagnes"))
                .andExpect(status().isNotFound());
    }

    @Test
    void comparerCandidats_shouldReturnComparison_whenCandidatesExist() throws Exception {
        when(candidatService.obtenirDetailCandidat("candidat1-uuid")).thenReturn(new CandidatService.CandidatDetailDTO());
        when(candidatService.obtenirDetailCandidat("candidat2-uuid")).thenReturn(new CandidatService.CandidatDetailDTO());

        mockMvc.perform(get("/api/public/candidats/comparer")
                .param("candidat1Id", "candidat1-uuid")
                .param("candidat2Id", "candidat2-uuid"))
                .andExpect(status().isOk());
    }

    @Test
    void comparerCandidats_shouldReturnBadRequest_whenOneCandidateDoesNotExist() throws Exception {
        when(candidatService.obtenirDetailCandidat("candidat1-uuid")).thenReturn(new CandidatService.CandidatDetailDTO());
        when(candidatService.obtenirDetailCandidat("candidat2-uuid")).thenThrow(new RuntimeException());

        mockMvc.perform(get("/api/public/candidats/comparer")
                .param("candidat1Id", "candidat1-uuid")
                .param("candidat2Id", "candidat2-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void filtrerParNombreCampagnes_shouldReturnFilteredCandidats() throws Exception {
        when(candidatService.listerTousCandidats()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/public/candidats/filtre/par-campagnes").param("minCampagnes", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void filtrerParVotes_shouldReturnFilteredCandidats() throws Exception {
        when(candidatService.obtenirClassementCandidats()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/public/candidats/filtre/par-votes").param("minVotes", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void healthCheck_shouldReturnOk() throws Exception {
        when(candidatService.listerTousCandidats()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/public/candidats/health"))
                .andExpect(status().isOk());
    }
}
