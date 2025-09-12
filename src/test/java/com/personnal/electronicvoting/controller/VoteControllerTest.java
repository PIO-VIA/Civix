package com.personnal.electronicvoting.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personnal.electronicvoting.model.Electeur;
import com.personnal.electronicvoting.service.AuthService;
import com.personnal.electronicvoting.service.VoteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VoteController.class)
class VoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VoteService voteService;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void effectuerVote_shouldReturnOk_whenVoteIsValid() throws Exception {
        Electeur electeur = new Electeur();
        electeur.setExternalIdElecteur("electeur-uuid");

        when(authService.verifierTokenElecteur(anyString())).thenReturn(true);
        when(authService.obtenirElecteurDepuisToken(anyString())).thenReturn(electeur);
        when(voteService.effectuerVote(anyString(), anyString())).thenReturn(new com.personnal.electronicvoting.dto.VoteDTO());

        mockMvc.perform(post("/api/votes/effectuer").param("candidatId", "candidat-uuid")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk());
    }

    @Test
    void effectuerVote_shouldReturnBadRequest_whenVoteIsInvalid() throws Exception {
        Electeur electeur = new Electeur();
        electeur.setExternalIdElecteur("electeur-uuid");

        when(authService.verifierTokenElecteur(anyString())).thenReturn(true);
        when(authService.obtenirElecteurDepuisToken(anyString())).thenReturn(electeur);
        when(voteService.effectuerVote(anyString(), anyString())).thenThrow(new RuntimeException());

        mockMvc.perform(post("/api/votes/effectuer").param("candidatId", "candidat-uuid")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void consulterResultats_shouldReturnResults() throws Exception {
        when(voteService.obtenirResultatsVotes()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/votes/resultats"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenirGagnant_shouldReturnWinner() throws Exception {
        when(voteService.obtenirGagnant()).thenReturn(new VoteService.ResultatVoteDTO());

        mockMvc.perform(get("/api/votes/gagnant"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenirStatutVote_shouldReturnStatus_whenTokenIsValid() throws Exception {
        Electeur electeur = new Electeur();
        electeur.setExternalIdElecteur("electeur-uuid");

        when(authService.verifierTokenElecteur(anyString())).thenReturn(true);
        when(authService.obtenirElecteurDepuisToken(anyString())).thenReturn(electeur);
        when(voteService.obtenirStatutVoteElecteur(anyString())).thenReturn(new VoteService.StatutVoteElecteurDTO());

        mockMvc.perform(get("/api/votes/statut")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk());
    }

    @Test
    void verifierPeutVoter_shouldReturnStatus_whenTokenIsValid() throws Exception {
        Electeur electeur = new Electeur();
        electeur.setExternalIdElecteur("electeur-uuid");

        when(authService.verifierTokenElecteur(anyString())).thenReturn(true);
        when(authService.obtenirElecteurDepuisToken(anyString())).thenReturn(electeur);
        when(voteService.electeurPeutVoter(anyString())).thenReturn(true);

        mockMvc.perform(get("/api/votes/peut-voter")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenirStatistiques_shouldReturnStatistics() throws Exception {
        when(voteService.obtenirStatistiquesGenerales()).thenReturn(new VoteService.StatistiquesVoteDTO());

        mockMvc.perform(get("/api/votes/statistiques"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenirRepartitionTemporelle_shouldReturnTemporalRepartition() throws Exception {
        when(voteService.obtenirRepartitionTemporelle()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/votes/repartition-temporelle"))
                .andExpect(status().isOk());
    }

    @Test
    void previsualiserVote_shouldReturnPreview_whenTokenIsValid() throws Exception {
        Electeur electeur = new Electeur();
        electeur.setExternalIdElecteur("electeur-uuid");

        when(authService.verifierTokenElecteur(anyString())).thenReturn(true);
        when(authService.obtenirElecteurDepuisToken(anyString())).thenReturn(electeur);
        when(voteService.electeurPeutVoter(anyString())).thenReturn(true);

        mockMvc.perform(post("/api/votes/previsualiser").param("candidatId", "candidat-uuid")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk());
    }
}
