package com.personnal.electronicvoting.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personnal.electronicvoting.dto.request.VoterElectionRequest;
import com.personnal.electronicvoting.model.Electeur;
import com.personnal.electronicvoting.service.AuthService;
import com.personnal.electronicvoting.service.ElectionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ElectionController.class)
class ElectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ElectionService electionService;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listerToutesElections_shouldReturnAllElections() throws Exception {
        when(electionService.listerToutesElections()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/elections"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenirElection_shouldReturnElection_whenElectionExists() throws Exception {
        when(electionService.obtenirElection("election-uuid")).thenReturn(new com.personnal.electronicvoting.dto.ElectionDTO());

        mockMvc.perform(get("/api/elections/election-uuid"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenirElection_shouldReturnNotFound_whenElectionDoesNotExist() throws Exception {
        when(electionService.obtenirElection("election-uuid")).thenThrow(new RuntimeException());

        mockMvc.perform(get("/api/elections/election-uuid"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listerElectionsDisponibles_shouldReturnAvailableElections_whenTokenIsValid() throws Exception {
        Electeur electeur = new Electeur();
        electeur.setExternalIdElecteur("electeur-uuid");

        when(authService.verifierTokenElecteur(anyString())).thenReturn(true);
        when(authService.obtenirElecteurDepuisToken(anyString())).thenReturn(electeur);
        when(electionService.listerElectionsDisponiblesPourElecteur(anyString())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/elections/disponibles")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk());
    }

    @Test
    void listerElectionsDisponibles_shouldReturnUnauthorized_whenTokenIsInvalid() throws Exception {
        when(authService.verifierTokenElecteur(anyString())).thenReturn(false);

        mockMvc.perform(get("/api/elections/disponibles")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void voterPourElection_shouldReturnOk_whenVoteIsValid() throws Exception {
        Electeur electeur = new Electeur();
        electeur.setExternalIdElecteur("electeur-uuid");

        VoterElectionRequest request = new VoterElectionRequest();
        request.setCandidatId("candidat-uuid");

        when(authService.verifierTokenElecteur(anyString())).thenReturn(true);
        when(authService.obtenirElecteurDepuisToken(anyString())).thenReturn(electeur);
        when(electionService.voterPourElection(any(VoterElectionRequest.class), anyString())).thenReturn(new com.personnal.electronicvoting.dto.VoteElectionDTO());

        mockMvc.perform(post("/api/elections/election-uuid/voter")
                .header("Authorization", "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void voterPourElection_shouldReturnUnauthorized_whenTokenIsInvalid() throws Exception {
        VoterElectionRequest request = new VoterElectionRequest();
        request.setCandidatId("candidat-uuid");

        when(authService.verifierTokenElecteur(anyString())).thenReturn(false);

        mockMvc.perform(post("/api/elections/election-uuid/voter")
                .header("Authorization", "Bearer invalid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void voterPourElection_shouldReturnBadRequest_whenVoteIsInvalid() throws Exception {
        Electeur electeur = new Electeur();
        electeur.setExternalIdElecteur("electeur-uuid");

        VoterElectionRequest request = new VoterElectionRequest();
        request.setCandidatId("candidat-uuid");

        when(authService.verifierTokenElecteur(anyString())).thenReturn(true);
        when(authService.obtenirElecteurDepuisToken(anyString())).thenReturn(electeur);
        when(electionService.voterPourElection(any(VoterElectionRequest.class), anyString())).thenThrow(new RuntimeException());

        mockMvc.perform(post("/api/elections/election-uuid/voter")
                .header("Authorization", "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtenirResultatsElection_shouldReturnResults_whenResultsAreAvailable() throws Exception {
        when(electionService.obtenirResultatsElection("election-uuid")).thenReturn(new ElectionService.ResultatsElectionDTO());

        mockMvc.perform(get("/api/elections/election-uuid/resultats"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenirResultatsElection_shouldReturnBadRequest_whenResultsNotAvailable() throws Exception {
        when(electionService.obtenirResultatsElection("election-uuid")).thenThrow(new RuntimeException());

        mockMvc.perform(get("/api/elections/election-uuid/resultats"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void healthCheck_shouldReturnOk() throws Exception {
        when(electionService.listerToutesElections()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/elections/health"))
                .andExpect(status().isOk());
    }
}
