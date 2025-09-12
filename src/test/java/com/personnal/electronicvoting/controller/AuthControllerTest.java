package com.personnal.electronicvoting.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personnal.electronicvoting.dto.request.LoginRequest;
import com.personnal.electronicvoting.dto.response.AuthResponse;
import com.personnal.electronicvoting.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void loginElecteur_shouldReturnOk_whenCredentialsAreValid() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setMotDePasse("password");

        AuthResponse authResponse = AuthResponse.builder().token("test-token").build();

        when(authService.authentifierElecteur(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/electeur/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void loginElecteur_shouldReturnBadRequest_whenCredentialsAreInvalid() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setMotDePasse("wrong-password");

        when(authService.authentifierElecteur(any(LoginRequest.class))).thenReturn(null);

        mockMvc.perform(post("/api/auth/electeur/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginAdministrateur_shouldReturnOk_whenCredentialsAreValid() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@example.com");
        loginRequest.setMotDePasse("password");

        AuthResponse authResponse = AuthResponse.builder().token("admin-token").build();

        when(authService.authentifierAdministrateur(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/admin/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void loginAdministrateur_shouldReturnBadRequest_whenCredentialsAreInvalid() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@example.com");
        loginRequest.setMotDePasse("wrong-password");

        when(authService.authentifierAdministrateur(any(LoginRequest.class))).thenThrow(new RuntimeException());

        mockMvc.perform(post("/api/auth/admin/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changerMotDePasseElecteur_shouldReturnOk_whenRequestIsValid() throws Exception {
        com.personnal.electronicvoting.dto.request.ChangePasswordRequest request = new com.personnal.electronicvoting.dto.request.ChangePasswordRequest();
        request.setAncienMotDePasse("oldPassword");
        request.setNouveauMotDePasse("newPassword");

        AuthResponse authResponse = AuthResponse.builder().token("new-token").build();

        when(authService.changerMotDePasseElecteur(anyString(), anyString(), anyString())).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/electeur/change-password")
                .header("Authorization", "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void changerMotDePasseElecteur_shouldReturnBadRequest_whenTokenIsInvalid() throws Exception {
        com.personnal.electronicvoting.dto.request.ChangePasswordRequest request = new com.personnal.electronicvoting.dto.request.ChangePasswordRequest();
        request.setAncienMotDePasse("oldPassword");
        request.setNouveauMotDePasse("newPassword");

        when(authService.changerMotDePasseElecteur(anyString(), anyString(), anyString())).thenThrow(new RuntimeException());

        mockMvc.perform(post("/api/auth/electeur/change-password")
                .header("Authorization", "Bearer invalid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void verifierTokenElecteur_shouldReturnTrue_whenTokenIsValid() throws Exception {
        when(authService.verifierTokenElecteur(anyString())).thenReturn(true);

        mockMvc.perform(get("/api/auth/electeur/verify")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk());
    }

    @Test
    void verifierTokenElecteur_shouldReturnFalse_whenTokenIsInvalid() throws Exception {
        when(authService.verifierTokenElecteur(anyString())).thenReturn(false);

        mockMvc.perform(get("/api/auth/electeur/verify")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isOk());
    }

    @Test
    void verifierTokenAdmin_shouldReturnTrue_whenTokenIsValid() throws Exception {
        when(authService.verifierTokenAdmin(anyString())).thenReturn(true);

        mockMvc.perform(get("/api/auth/admin/verify")
                .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk());
    }

    @Test
    void verifierTokenAdmin_shouldReturnFalse_whenTokenIsInvalid() throws Exception {
        when(authService.verifierTokenAdmin(anyString())).thenReturn(false);

        mockMvc.perform(get("/api/auth/admin/verify")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isOk());
    }

    @Test
    void getSessionElecteur_shouldReturnSessionInfo_whenTokenIsValid() throws Exception {
        com.personnal.electronicvoting.model.Electeur electeur = new com.personnal.electronicvoting.model.Electeur();
        electeur.setExternalIdElecteur("electeur-uuid");

        when(authService.verifierTokenElecteur(anyString())).thenReturn(true);
        when(authService.obtenirElecteurDepuisToken(anyString())).thenReturn(electeur);

        mockMvc.perform(get("/api/auth/electeur/session")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk());
    }

    @Test
    void getSessionElecteur_shouldReturnUnauthorized_whenTokenIsInvalid() throws Exception {
        when(authService.verifierTokenElecteur(anyString())).thenReturn(false);

        mockMvc.perform(get("/api/auth/electeur/session")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getSessionAdmin_shouldReturnSessionInfo_whenTokenIsValid() throws Exception {
        com.personnal.electronicvoting.model.Administrateur admin = new com.personnal.electronicvoting.model.Administrateur();
        admin.setExternalIdAdministrateur("admin-uuid");

        when(authService.verifierTokenAdmin(anyString())).thenReturn(true);
        when(authService.obtenirAdminDepuisToken(anyString())).thenReturn(admin);

        mockMvc.perform(get("/api/auth/admin/session")
                .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk());
    }

    @Test
    void getSessionAdmin_shouldReturnUnauthorized_whenTokenIsInvalid() throws Exception {
        when(authService.verifierTokenAdmin(anyString())).thenReturn(false);

        mockMvc.perform(get("/api/auth/admin/session")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk());
    }
}
