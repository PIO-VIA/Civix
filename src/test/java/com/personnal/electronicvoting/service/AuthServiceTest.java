package com.personnal.electronicvoting.service;

import com.personnal.electronicvoting.dto.request.LoginRequest;
import com.personnal.electronicvoting.dto.response.AuthResponse;
import com.personnal.electronicvoting.model.Administrateur;
import com.personnal.electronicvoting.model.Electeur;
import com.personnal.electronicvoting.repository.AdministrateurRepository;
import com.personnal.electronicvoting.repository.ElecteurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private ElecteurRepository electeurRepository;

    @Mock
    private AdministrateurRepository administrateurRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private Electeur electeur;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        electeur = new Electeur();
        electeur.setExternalIdElecteur("test-uuid");
        electeur.setUsername("testuser");
        electeur.setEmail("test@example.com");
        electeur.setMotDePasse("encodedPassword");
        electeur.setAVote(false);

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setMotDePasse("password");
    }

    @Test
    void authentifierElecteur_shouldReturnAuthResponse_whenCredentialsAreValid() {
        when(electeurRepository.findByEmail(anyString())).thenReturn(Optional.of(electeur));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        AuthResponse authResponse = authService.authentifierElecteur(loginRequest);

        assertNotNull(authResponse);
        assertEquals(electeur.getExternalIdElecteur(), authResponse.getUserId());
        assertEquals(electeur.getUsername(), authResponse.getUsername());
        assertEquals(electeur.getEmail(), authResponse.getEmail());
        assertEquals("ELECTEUR", authResponse.getRole());
        assertNotNull(authResponse.getToken());
    }

    @Test
    void authentifierElecteur_shouldThrowException_whenEmailDoesNotExist() {
        when(electeurRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.authentifierElecteur(loginRequest);
        });

        assertEquals("Identifiants invalides", exception.getMessage());
    }

    @Test
    void authentifierElecteur_shouldReturnNull_whenPasswordIsIncorrect() {
        when(electeurRepository.findByEmail(anyString())).thenReturn(Optional.of(electeur));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        AuthResponse authResponse = authService.authentifierElecteur(loginRequest);

        assertNull(authResponse);
    }

    @Test
    void authentifierAdministrateur_shouldReturnAuthResponse_whenCredentialsAreValid() {
        Administrateur admin = new Administrateur();
        admin.setExternalIdAdministrateur("admin-uuid");
        admin.setUsername("adminuser");
        admin.setEmail("admin@example.com");
        admin.setMotDePasse("encodedPassword");

        LoginRequest adminLoginRequest = new LoginRequest();
        adminLoginRequest.setEmail("admin@example.com");
        adminLoginRequest.setMotDePasse("password");

        when(administrateurRepository.findByEmail(anyString())).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        AuthResponse authResponse = authService.authentifierAdministrateur(adminLoginRequest);

        assertNotNull(authResponse);
        assertEquals(admin.getExternalIdAdministrateur(), authResponse.getUserId());
        assertEquals(admin.getUsername(), authResponse.getUsername());
        assertEquals(admin.getEmail(), authResponse.getEmail());
        assertEquals("ADMIN", authResponse.getRole());
        assertNotNull(authResponse.getToken());
    }

    @Test
    void authentifierAdministrateur_shouldThrowException_whenEmailDoesNotExist() {
        LoginRequest adminLoginRequest = new LoginRequest();
        adminLoginRequest.setEmail("admin@example.com");
        adminLoginRequest.setMotDePasse("password");

        when(administrateurRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.authentifierAdministrateur(adminLoginRequest);
        });

        assertEquals("Identifiants invalides", exception.getMessage());
    }

    @Test
    void authentifierAdministrateur_shouldThrowException_whenPasswordIsIncorrect() {
        Administrateur admin = new Administrateur();
        admin.setEmail("admin@example.com");
        admin.setMotDePasse("encodedPassword");

        LoginRequest adminLoginRequest = new LoginRequest();
        adminLoginRequest.setEmail("admin@example.com");
        adminLoginRequest.setMotDePasse("password");

        when(administrateurRepository.findByEmail(anyString())).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.authentifierAdministrateur(adminLoginRequest);
        });

        assertEquals("Identifiants invalides", exception.getMessage());
    }

    @Test
    void verifierTokenElecteur_shouldReturnTrue_whenTokenIsValid() {
        String token = "ELECTEUR-test-uuid-123456789";
        when(electeurRepository.findByExternalIdElecteur("test-uuid")).thenReturn(Optional.of(electeur));

        assertTrue(authService.verifierTokenElecteur(token));
    }

    @Test
    void verifierTokenElecteur_shouldReturnFalse_whenTokenIsInvalid() {
        assertFalse(authService.verifierTokenElecteur(null));
        assertFalse(authService.verifierTokenElecteur(""));
        assertFalse(authService.verifierTokenElecteur("INVALID-TOKEN"));
        assertFalse(authService.verifierTokenElecteur("ELECTEUR-test-uuid")); // No timestamp
    }

    @Test
    void verifierTokenElecteur_shouldReturnFalse_whenUserDoesNotExist() {
        String token = "ELECTEUR-non-existent-uuid-123456789";
        when(electeurRepository.findByExternalIdElecteur("non-existent-uuid")).thenReturn(Optional.empty());

        assertFalse(authService.verifierTokenElecteur(token));
    }

    @Test
    void verifierTokenAdmin_shouldReturnTrue_whenTokenIsValid() {
        String token = "ADMIN-admin-uuid-123456789";
        Administrateur admin = new Administrateur();
        admin.setExternalIdAdministrateur("admin-uuid");
        when(administrateurRepository.findByExternalIdAdministrateur("admin-uuid")).thenReturn(Optional.of(admin));

        assertTrue(authService.verifierTokenAdmin(token));
    }

    @Test
    void verifierTokenAdmin_shouldReturnFalse_whenTokenIsInvalid() {
        assertFalse(authService.verifierTokenAdmin(null));
        assertFalse(authService.verifierTokenAdmin(""));
        assertFalse(authService.verifierTokenAdmin("INVALID-TOKEN"));
        assertFalse(authService.verifierTokenAdmin("ADMIN-admin-uuid")); // No timestamp
    }

    @Test
    void verifierTokenAdmin_shouldReturnFalse_whenUserDoesNotExist() {
        String token = "ADMIN-non-existent-uuid-123456789";
        when(administrateurRepository.findByExternalIdAdministrateur("non-existent-uuid")).thenReturn(Optional.empty());

        assertFalse(authService.verifierTokenAdmin(token));
    }

    @Test
    void obtenirElecteurDepuisToken_shouldReturnElecteur_whenTokenIsValid() {
        String token = "ELECTEUR-test-uuid-123456789";
        when(electeurRepository.findByExternalIdElecteur("test-uuid")).thenReturn(Optional.of(electeur));

        Electeur result = authService.obtenirElecteurDepuisToken(token);

        assertNotNull(result);
        assertEquals(electeur.getExternalIdElecteur(), result.getExternalIdElecteur());
    }

    @Test
    void obtenirElecteurDepuisToken_shouldThrowException_whenTokenIsInvalid() {
        String token = "INVALID-TOKEN";

        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.obtenirElecteurDepuisToken(token);
        });

        assertEquals("Token électeur invalide", exception.getMessage());
    }

    @Test
    void obtenirAdminDepuisToken_shouldReturnAdmin_whenTokenIsValid() {
        String token = "ADMIN-admin-uuid-123456789";
        Administrateur admin = new Administrateur();
        admin.setExternalIdAdministrateur("admin-uuid");
        when(administrateurRepository.findByExternalIdAdministrateur("admin-uuid")).thenReturn(Optional.of(admin));

        Administrateur result = authService.obtenirAdminDepuisToken(token);

        assertNotNull(result);
        assertEquals(admin.getExternalIdAdministrateur(), result.getExternalIdAdministrateur());
    }

    @Test
    void obtenirAdminDepuisToken_shouldThrowException_whenTokenIsInvalid() {
        String token = "INVALID-TOKEN";

        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.obtenirAdminDepuisToken(token);
        });

        assertEquals("Token admin invalide", exception.getMessage());
    }

    @Test
    void changerMotDePasseElecteur_shouldChangePassword_whenOldPasswordIsCorrect() {
        String token = "ELECTEUR-test-uuid-123456789";
        String oldPassword = "password";
        String newPassword = "newPassword123!";

        when(electeurRepository.findByExternalIdElecteur("test-uuid")).thenReturn(Optional.of(electeur));
        when(passwordEncoder.matches(oldPassword, electeur.getMotDePasse())).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");

        AuthResponse authResponse = authService.changerMotDePasseElecteur(token, oldPassword, newPassword);

        assertNotNull(authResponse);
        assertEquals(electeur.getExternalIdElecteur(), authResponse.getUserId());
        assertFalse(authResponse.isPremierConnexion());
    }

    @Test
    void changerMotDePasseElecteur_shouldThrowException_whenOldPasswordIsIncorrect() {
        String token = "ELECTEUR-test-uuid-123456789";
        String oldPassword = "wrongPassword";
        String newPassword = "newPassword123!";

        when(electeurRepository.findByExternalIdElecteur("test-uuid")).thenReturn(Optional.of(electeur));
        when(passwordEncoder.matches(oldPassword, electeur.getMotDePasse())).thenReturn(false);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.changerMotDePasseElecteur(token, oldPassword, newPassword);
        });

        assertEquals("Ancien mot de passe incorrect", exception.getMessage());
    }

    @Test
    void changerMotDePasseElecteur_shouldThrowException_whenNewPasswordIsInvalid() {
        String token = "ELECTEUR-test-uuid-123456789";
        String oldPassword = "password";
        String newPassword = "short";

        when(electeurRepository.findByExternalIdElecteur("test-uuid")).thenReturn(Optional.of(electeur));
        when(passwordEncoder.matches(oldPassword, electeur.getMotDePasse())).thenReturn(true);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.changerMotDePasseElecteur(token, oldPassword, newPassword);
        });

        assertTrue(exception.getMessage().contains("Le mot de passe doit contenir au moins 8 caractères"));
    }
}
