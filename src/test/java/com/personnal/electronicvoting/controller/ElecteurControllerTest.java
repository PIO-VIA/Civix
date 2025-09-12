package com.personnal.electronicvoting.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personnal.electronicvoting.model.Electeur;
import com.personnal.electronicvoting.service.AuthService;
import com.personnal.electronicvoting.service.ElecteurService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ElecteurController.class)
class ElecteurControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ElecteurService electeurService;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void obtenirMonProfil_shouldReturnProfile_whenTokenIsValid() throws Exception {
        Electeur electeur = new Electeur();
        electeur.setExternalIdElecteur("electeur-uuid");

        when(authService.verifierTokenElecteur(anyString())).thenReturn(true);
        when(authService.obtenirElecteurDepuisToken(anyString())).thenReturn(electeur);
        when(electeurService.obtenirProfil(anyString())).thenReturn(new ElecteurService.ElecteurProfilDTO());

        mockMvc.perform(get("/api/electeur/profil")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenirMonProfil_shouldReturnUnauthorized_whenTokenIsInvalid() throws Exception {
        when(authService.verifierTokenElecteur(anyString())).thenReturn(false);

        mockMvc.perform(get("/api/electeur/profil")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changerMotDePasse_shouldReturnOk_whenRequestIsValid() throws Exception {
        com.personnal.electronicvoting.dto.request.ChangePasswordRequest request = new com.personnal.electronicvoting.dto.request.ChangePasswordRequest();
        request.setAncienMotDePasse("oldPassword");
        request.setNouveauMotDePasse("newPassword");

        com.personnal.electronicvoting.dto.response.AuthResponse authResponse = com.personnal.electronicvoting.dto.response.AuthResponse.builder().userId("electeur-uuid").build();

        when(authService.changerMotDePasseElecteur(anyString(), anyString(), anyString())).thenReturn(authResponse);

        mockMvc.perform(put("/api/electeur/profil/mot-de-passe")
                .header("Authorization", "Bearer test-token")
                .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void changerMotDePasse_shouldReturnBadRequest_whenRequestIsInvalid() throws Exception {
        com.personnal.electronicvoting.dto.request.ChangePasswordRequest request = new com.personnal.electronicvoting.dto.request.ChangePasswordRequest();
        request.setAncienMotDePasse("oldPassword");
        request.setNouveauMotDePasse("newPassword");

        when(authService.changerMotDePasseElecteur(anyString(), anyString(), anyString())).thenThrow(new RuntimeException());

        mockMvc.perform(put("/api/electeur/profil/mot-de-passe")
                .header("Authorization", "Bearer test-token")
                .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtenirTableauBord_shouldReturnDashboard_whenTokenIsValid() throws Exception {
        Electeur electeur = new Electeur();
        electeur.setExternalIdElecteur("electeur-uuid");

        when(authService.verifierTokenElecteur(anyString())).thenReturn(true);
        when(authService.obtenirElecteurDepuisToken(anyString())).thenReturn(electeur);
        when(electeurService.obtenirTableauBord(anyString())).thenReturn(new ElecteurService.TableauBordElecteurDTO());

        mockMvc.perform(get("/api/electeur/dashboard")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenirTableauBord_shouldReturnUnauthorized_whenTokenIsInvalid() throws Exception {
        when(authService.verifierTokenElecteur(anyString())).thenReturn(false);

        mockMvc.perform(get("/api/electeur/dashboard")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void consulterCandidats_shouldReturnCandidates_whenTokenIsValid() throws Exception {
        Electeur electeur = new Electeur();
        electeur.setExternalIdElecteur("electeur-uuid");

        when(authService.verifierTokenElecteur(anyString())).thenReturn(true);
        when(authService.obtenirElecteurDepuisToken(anyString())).thenReturn(electeur);
        when(electeurService.consulterCandidats(anyString())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/electeur/candidats")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk());
    }

    @Test
    void consulterCandidats_shouldReturnUnauthorized_whenTokenIsInvalid() throws Exception {
        when(authService.verifierTokenElecteur(anyString())).thenReturn(false);

        mockMvc.perform(get("/api/electeur/candidats")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void consulterCampagnesCandidat_shouldReturnCampaigns_whenTokenIsValid() throws Exception {
        Electeur electeur = new Electeur();
        electeur.setExternalIdElecteur("electeur-uuid");

        when(authService.verifierTokenElecteur(anyString())).thenReturn(true);
        when(authService.obtenirElecteurDepuisToken(anyString())).thenReturn(electeur);
        when(electeurService.consulterCampagnesCandidat(anyString(), anyString())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/electeur/candidats/candidat-uuid/campagnes")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk());
    }

    @Test
    void consulterCampagnesCandidat_shouldReturnUnauthorized_whenTokenIsInvalid() throws Exception {
        when(authService.verifierTokenElecteur(anyString())).thenReturn(false);

        mockMvc.perform(get("/api/electeur/candidats/candidat-uuid/campagnes")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void consulterResultats_shouldReturnResults_whenTokenIsValid() throws Exception {
        Electeur electeur = new Electeur();
        electeur.setExternalIdElecteur("electeur-uuid");

        when(authService.verifierTokenElecteur(anyString())).thenReturn(true);
        when(authService.obtenirElecteurDepuisToken(anyString())).thenReturn(electeur);
        when(electeurService.consulterResultatsPartiels(anyString())).thenReturn(new ElecteurService.ResultatsPartielsDTO());

        mockMvc.perform(get("/api/electeur/resultats")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk());
    }

    @Test
    void consulterResultats_shouldReturnUnauthorized_whenTokenIsInvalid() throws Exception {
        when(authService.verifierTokenElecteur(anyString())).thenReturn(false);

        mockMvc.perform(get("/api/electeur/resultats")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void obtenirHistorique_shouldReturnHistory_whenTokenIsValid() throws Exception {
        Electeur electeur = new Electeur();
        electeur.setExternalIdElecteur("electeur-uuid");

        when(authService.verifierTokenElecteur(anyString())).thenReturn(true);
        when(authService.obtenirElecteurDepuisToken(anyString())).thenReturn(electeur);

        mockMvc.perform(get("/api/electeur/historique")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenirHistorique_shouldReturnUnauthorized_whenTokenIsInvalid() throws Exception {
        when(authService.verifierTokenElecteur(anyString())).thenReturn(false);

        mockMvc.perform(get("/api/electeur/historique")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void obtenirNotifications_shouldReturnNotifications_whenTokenIsValid() throws Exception {
        Electeur electeur = new Electeur();
        electeur.setExternalIdElecteur("electeur-uuid");

        when(authService.verifierTokenElecteur(anyString())).thenReturn(true);
        when(authService.obtenirElecteurDepuisToken(anyString())).thenReturn(electeur);

        mockMvc.perform(get("/api/electeur/notifications")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenirNotifications_shouldReturnUnauthorized_whenTokenIsInvalid() throws Exception {
        when(authService.verifierTokenElecteur(anyString())).thenReturn(false);

        mockMvc.perform(get("/api/electeur/notifications")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void obtenirAide_shouldReturnHelpInfo() throws Exception {
        mockMvc.perform(get("/api/electeur/aide"))
                .andExpect(status().isOk());
    }
}
