package com.personnal.electronicvoting.service;

import com.personnal.electronicvoting.dto.ElecteurDTO;
import com.personnal.electronicvoting.dto.request.ChangePasswordRequest;
import com.personnal.electronicvoting.mapper.CampagneMapper;
import com.personnal.electronicvoting.mapper.CandidatMapper;
import com.personnal.electronicvoting.mapper.UserMapper;
import com.personnal.electronicvoting.model.Electeur;
import com.personnal.electronicvoting.repository.CampagneRepository;
import com.personnal.electronicvoting.repository.CandidatRepository;
import com.personnal.electronicvoting.repository.ElecteurRepository;
import com.personnal.electronicvoting.repository.VoteRepository;
import com.personnal.electronicvoting.util.PasswordGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ElecteurServiceTest {

    @Mock
    private ElecteurRepository electeurRepository;

    @Mock
    private CandidatRepository candidatRepository;

    @Mock
    private CampagneRepository campagneRepository;

    @Mock
    private VoteRepository voteRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private CandidatMapper candidatMapper;

    @Mock
    private CampagneMapper campagneMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordGenerator passwordGenerator;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ElecteurService electeurService;

    private Electeur electeur;

    @BeforeEach
    void setUp() {
        electeur = new Electeur();
        electeur.setExternalIdElecteur("test-uuid");
        electeur.setAVote(false);
    }

    @Test
    void obtenirProfil_shouldReturnProfile_whenElecteurExists() {
        when(electeurRepository.findByExternalIdElecteur(anyString())).thenReturn(Optional.of(electeur));
        when(electeurRepository.count()).thenReturn(10L);
        when(electeurRepository.findByaVoteTrue()).thenReturn(Collections.nCopies(5, new Electeur()));
        when(userMapper.toDTO(any(Electeur.class))).thenReturn(new ElecteurDTO());

        ElecteurService.ElecteurProfilDTO profil = electeurService.obtenirProfil("test-uuid");

        assertNotNull(profil);
        assertEquals(50.0, profil.getTauxParticipationGlobal());
    }

    @Test
    void obtenirProfil_shouldThrowException_whenElecteurDoesNotExist() {
        when(electeurRepository.findByExternalIdElecteur(anyString())).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            electeurService.obtenirProfil("test-uuid");
        });

        assertEquals("Électeur non trouvé", exception.getMessage());
    }

    @Test
    void changerMotDePasse_shouldChangePassword_whenOldPasswordIsCorrect() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setAncienMotDePasse("oldPassword");
        request.setNouveauMotDePasse("newPassword");

        when(electeurRepository.findByExternalIdElecteur(anyString())).thenReturn(Optional.of(electeur));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        electeurService.changerMotDePasse("test-uuid", request);

        // No exception thrown means success
    }

    @Test
    void changerMotDePasse_shouldThrowException_whenElecteurDoesNotExist() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setAncienMotDePasse("oldPassword");
        request.setNouveauMotDePasse("newPassword");

        when(electeurRepository.findByExternalIdElecteur(anyString())).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            electeurService.changerMotDePasse("test-uuid", request);
        });

        assertEquals("Électeur non trouvé", exception.getMessage());
    }

    @Test
    void changerMotDePasse_shouldThrowException_whenOldPasswordIsIncorrect() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setAncienMotDePasse("wrongPassword");
        request.setNouveauMotDePasse("newPassword");

        when(electeurRepository.findByExternalIdElecteur(anyString())).thenReturn(Optional.of(electeur));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            electeurService.changerMotDePasse("test-uuid", request);
        });

        assertEquals("Ancien mot de passe incorrect", exception.getMessage());
    }

    @Test
    void consulterCandidats_shouldReturnCandidates_whenElecteurExists() {
        when(electeurRepository.findByExternalIdElecteur(anyString())).thenReturn(Optional.of(electeur));
        when(candidatRepository.findAll()).thenReturn(Collections.emptyList());

        var result = electeurService.consulterCandidats("test-uuid");

        assertNotNull(result);
    }

    @Test
    void consulterCandidats_shouldThrowException_whenElecteurDoesNotExist() {
        when(electeurRepository.findByExternalIdElecteur(anyString())).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            electeurService.consulterCandidats("test-uuid");
        });

        assertEquals("Électeur non trouvé", exception.getMessage());
    }

    @Test
    void consulterCampagnesCandidat_shouldReturnCampaigns_whenElecteurAndCandidatExist() {
        when(electeurRepository.findByExternalIdElecteur(anyString())).thenReturn(Optional.of(electeur));
        when(candidatRepository.findByExternalIdCandidat(anyString())).thenReturn(Optional.of(new com.personnal.electronicvoting.model.Candidat()));
        when(campagneRepository.findByCandidat_ExternalIdCandidat(anyString())).thenReturn(Collections.emptyList());

        var result = electeurService.consulterCampagnesCandidat("test-uuid", "candidat-uuid");

        assertNotNull(result);
    }

    @Test
    void consulterCampagnesCandidat_shouldThrowException_whenElecteurDoesNotExist() {
        when(electeurRepository.findByExternalIdElecteur(anyString())).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            electeurService.consulterCampagnesCandidat("test-uuid", "candidat-uuid");
        });

        assertEquals("Électeur non trouvé", exception.getMessage());
    }

    @Test
    void consulterCampagnesCandidat_shouldThrowException_whenCandidatDoesNotExist() {
        when(electeurRepository.findByExternalIdElecteur(anyString())).thenReturn(Optional.of(electeur));
        when(candidatRepository.findByExternalIdCandidat(anyString())).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            electeurService.consulterCampagnesCandidat("test-uuid", "candidat-uuid");
        });

        assertEquals("Candidat non trouvé", exception.getMessage());
    }

    @Test
    void consulterResultatsPartiels_shouldReturnResults_whenElecteurExists() {
        when(electeurRepository.findByExternalIdElecteur(anyString())).thenReturn(Optional.of(electeur));
        when(voteRepository.count()).thenReturn(5L);
        when(electeurRepository.count()).thenReturn(10L);
        when(candidatRepository.findAll()).thenReturn(Collections.emptyList());

        var result = electeurService.consulterResultatsPartiels("test-uuid");

        assertNotNull(result);
        assertEquals(50.0, result.getTauxParticipation());
    }

    @Test
    void consulterResultatsPartiels_shouldThrowException_whenElecteurDoesNotExist() {
        when(electeurRepository.findByExternalIdElecteur(anyString())).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            electeurService.consulterResultatsPartiels("test-uuid");
        });

        assertEquals("Électeur non trouvé", exception.getMessage());
    }

    @Test
    void listerTous_shouldReturnAllElecteurs() {
        when(electeurRepository.findAll()).thenReturn(Collections.emptyList());

        var result = electeurService.listerTous();

        assertNotNull(result);
    }

    @Test
    void marquerCommeAyantVote_shouldMarkElecteur_whenElecteurExists() {
        when(electeurRepository.findByExternalIdElecteur(anyString())).thenReturn(Optional.of(electeur));

        electeurService.marquerCommeAyantVote("test-uuid");

        assertTrue(electeur.isAVote());
    }

    @Test
    void marquerCommeAyantVote_shouldThrowException_whenElecteurDoesNotExist() {
        when(electeurRepository.findByExternalIdElecteur(anyString())).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            electeurService.marquerCommeAyantVote("test-uuid");
        });

        assertEquals("Électeur non trouvé", exception.getMessage());
    }

    @Test
    void trouverParExternalId_shouldReturnElecteur_whenElecteurExists() {
        when(electeurRepository.findByExternalIdElecteur(anyString())).thenReturn(Optional.of(electeur));
        when(userMapper.toDTO(any(Electeur.class))).thenReturn(new ElecteurDTO());

        Optional<ElecteurDTO> result = electeurService.trouverParExternalId("test-uuid");

        assertTrue(result.isPresent());
    }

    @Test
    void trouverParExternalId_shouldReturnEmpty_whenElecteurDoesNotExist() {
        when(electeurRepository.findByExternalIdElecteur(anyString())).thenReturn(Optional.empty());

        Optional<ElecteurDTO> result = electeurService.trouverParExternalId("test-uuid");

        assertFalse(result.isPresent());
    }

    @Test
    void obtenirTableauBord_shouldReturnDashboard_whenElecteurExists() {
        when(electeurRepository.findByExternalIdElecteur(anyString())).thenReturn(Optional.of(electeur));
        when(candidatRepository.count()).thenReturn(5L);
        when(campagneRepository.count()).thenReturn(10L);
        when(voteRepository.count()).thenReturn(2L);
        when(electeurRepository.count()).thenReturn(20L);
        when(candidatRepository.findAllOrderByVoteCountDesc()).thenReturn(Collections.emptyList());

        var result = electeurService.obtenirTableauBord("test-uuid");

        assertNotNull(result);
        assertEquals(5L, result.getNombreCandidats());
    }

    @Test
    void obtenirTableauBord_shouldThrowException_whenElecteurDoesNotExist() {
        when(electeurRepository.findByExternalIdElecteur(anyString())).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            electeurService.obtenirTableauBord("test-uuid");
        });

        assertEquals("Électeur non trouvé", exception.getMessage());
    }
}
