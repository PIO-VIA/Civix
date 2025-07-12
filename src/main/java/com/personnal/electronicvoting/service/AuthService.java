package com.personnal.electronicvoting.service;

import com.personnal.electronicvoting.dto.request.LoginRequest;
import com.personnal.electronicvoting.dto.response.AuthResponse;
import com.personnal.electronicvoting.model.*;
import com.personnal.electronicvoting.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuthService {

    private final ElecteurRepository electeurRepository;
    private final AdministrateurRepository administrateurRepository;
    private final PasswordEncoder passwordEncoder;

    // ==================== AUTHENTIFICATION ÉLECTEUR ====================

    /**
     *  Authentification électeur avec identifiants reçus par email
     */
    public AuthResponse authentifierElecteur(LoginRequest request) {
        log.info(" Tentative connexion électeur - Email: {}", request.getEmail());

        try {
            //  Recherche électeur par email
            Electeur electeur = electeurRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> {
                        log.warn(" Tentative connexion email électeur inexistant: {}", request.getEmail());
                        return new RuntimeException("Identifiants invalides");
                    });

            //  Vérification mot de passe
            if (!passwordEncoder.matches(request.getMotDePasse(), electeur.getMotDePasse())) {
                log.warn(" Tentative connexion mot de passe électeur incorrect: {}", request.getEmail());
                return null;
            }

            log.info(" Connexion électeur réussie - ID: {}, Username: {}",
                    electeur.getExternalIdElecteur(), electeur.getUsername());

            //  Génération "token" (simplifié pour l'instant)
            String token = genererTokenElecteur(electeur);

            return AuthResponse.builder()
                    .userId(electeur.getExternalIdElecteur())
                    .username(electeur.getUsername())
                    .email(electeur.getEmail())
                    .role("ELECTEUR")
                    .token(token)
                    .aVote(electeur.isAVote())
                    .premierConnexion(isMotDePasseTemporaire(electeur))
                    .build();

        } catch (RuntimeException e) {
            throw e; // Re-lancer les erreurs métier
        } catch (Exception e) {
            log.error(" Erreur authentification électeur: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'authentification", e);
        }
    }

    // ==================== AUTHENTIFICATION ADMINISTRATEUR ====================

    /**
     *  Authentification administrateur
     */
    public AuthResponse authentifierAdministrateur(LoginRequest request) {
        log.info(" Tentative connexion admin - Email: {}", request.getEmail());

        try {
            // 🔍 Recherche admin par email
            Administrateur admin = administrateurRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> {
                        log.warn(" Tentative connexion email admin inexistant: {}", request.getEmail());
                        return new RuntimeException("Identifiants invalides");
                    });

            //  Vérification mot de passe
            if (!passwordEncoder.matches(request.getMotDePasse(), admin.getMotDePasse())) {
                log.warn(" Tentative connexion mot de passe admin incorrect: {}", request.getEmail());
                throw new RuntimeException("Identifiants invalides");
            }

            log.info(" Connexion admin réussie - ID: {}, Username: {}",
                    admin.getExternalIdAdministrateur(), admin.getUsername());

            //  Génération "token" admin
            String token = genererTokenAdmin(admin);

            return AuthResponse.builder()
                    .userId(admin.getExternalIdAdministrateur())
                    .username(admin.getUsername())
                    .email(admin.getEmail())
                    .role("ADMIN")
                    .token(token)
                    .aVote(null) // N/A pour admin
                    .premierConnexion(false) // Admins gèrent leurs mots de passe
                    .build();

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error(" Erreur authentification admin: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'authentification", e);
        }
    }

    // ==================== GESTION SESSIONS / TOKENS ====================

    /**
     * Vérifier si un token électeur est valide
     */
    /**
     * Vérifier si un token admin est valide (CORRIGÉ)
     */
    public boolean verifierTokenAdmin(String token) {
        try {
            if (token.startsWith("ADMIN-") && token.length() > 6) {
                // Extraire seulement l'UUID admin (pas le timestamp)
                String tokenBody = token.substring(6); // Enlever "ADMIN-"
                String[] parts = tokenBody.split("-");

                // Reconstituer l'UUID (5 parties séparées par des tirets)
                if (parts.length >= 5) {
                    String adminId = String.join("-", parts[0], parts[1], parts[2], parts[3], parts[4]);

                    boolean exists = administrateurRepository.findByExternalIdAdministrateur(adminId).isPresent();
                    log.debug("Vérification token admin - UUID: {}, Existe: {}", adminId, exists);
                    return exists;
                }
            }
            return false;
        } catch (Exception e) {
            log.debug("Token admin invalide: {}", token);
            return false;
        }
    }

    /**
     * ✅ Vérifier si un token électeur est valide (CORRIGÉ)
     */
    public boolean verifierTokenElecteur(String token) {
        try {
            if (token.startsWith("ELECTEUR-") && token.length() > 9) {
                // Extraire seulement l'UUID électeur (pas le timestamp)
                String tokenBody = token.substring(9); // Enlever "ELECTEUR-"
                String[] parts = tokenBody.split("-");

                // Reconstituer l'UUID (5 parties séparées par des tirets)
                if (parts.length >= 5) {
                    String electeurId = String.join("-", parts[0], parts[1], parts[2], parts[3], parts[4]);

                    boolean exists = electeurRepository.findByExternalIdElecteur(electeurId).isPresent();
                    log.debug("Vérification token électeur - UUID: {}, Existe: {}", electeurId, exists);
                    return exists;
                }
            }
            return false;
        } catch (Exception e) {
            log.debug("Token électeur invalide: {}", token);
            return false;
        }
    }

    /**
     *  Obtenir admin depuis token (CORRIGÉ)
     */
    public Administrateur obtenirAdminDepuisToken(String token) {
        if (!verifierTokenAdmin(token)) {
            throw new RuntimeException("Token admin invalide");
        }

        // Extraire l'UUID admin
        String tokenBody = token.substring(6); // Enlever "ADMIN-"
        String[] parts = tokenBody.split("-");
        String adminId = String.join("-", parts[0], parts[1], parts[2], parts[3], parts[4]);

        return administrateurRepository.findByExternalIdAdministrateur(adminId)
                .orElseThrow(() -> new RuntimeException("Administrateur non trouvé"));
    }

    /**
     * Obtenir électeur depuis token (CORRIGÉ)
     */
    public Electeur obtenirElecteurDepuisToken(String token) {
        if (!verifierTokenElecteur(token)) {
            throw new RuntimeException("Token électeur invalide");
        }

        // Extraire l'UUID électeur
        String tokenBody = token.substring(9); // Enlever "ELECTEUR-"
        String[] parts = tokenBody.split("-");
        String electeurId = String.join("-", parts[0], parts[1], parts[2], parts[3], parts[4]);

        return electeurRepository.findByExternalIdElecteur(electeurId)
                .orElseThrow(() -> new RuntimeException("Électeur non trouvé"));
    }
    // ==================== CHANGEMENT MOT DE PASSE ====================

    /**
     *  Changer mot de passe électeur (première connexion)
     */
    @Transactional
    public AuthResponse changerMotDePasseElecteur(String token, String ancienMotDePasse, String nouveauMotDePasse) {
        log.info(" Changement mot de passe électeur");

        try {
            Electeur electeur = obtenirElecteurDepuisToken(token);

            // Vérifier ancien mot de passe
            if (!passwordEncoder.matches(ancienMotDePasse, electeur.getMotDePasse())) {
                throw new RuntimeException("Ancien mot de passe incorrect");
            }

            // Validation nouveau mot de passe
            validerNouveauMotDePasse(nouveauMotDePasse);

            // Changer le mot de passe
            electeur.setMotDePasse(passwordEncoder.encode(nouveauMotDePasse));
            electeurRepository.save(electeur);

            log.info(" Mot de passe électeur changé - ID: {}", electeur.getExternalIdElecteur());

            // Retourner nouvelle auth response
            return AuthResponse.builder()
                    .userId(electeur.getExternalIdElecteur())
                    .username(electeur.getUsername())
                    .email(electeur.getEmail())
                    .role("ELECTEUR")
                    .token(genererTokenElecteur(electeur))
                    .aVote(electeur.isAVote())
                    .premierConnexion(false) // Plus première connexion
                    .build();

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error(" Erreur changement mot de passe électeur: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors du changement de mot de passe", e);
        }
    }

    // ==================== UTILITAIRES ====================

    /**
     *  Génération token électeur (simplifié)
     *  Future : JWT avec expiration, claims, etc.
     */
    private String genererTokenElecteur(Electeur electeur) {
        // Token simplifié pour l'apprentissage
        return "ELECTEUR-" + electeur.getExternalIdElecteur() + "-" + System.currentTimeMillis();
    }

    /**
     *  Génération token admin
     */
    private String genererTokenAdmin(Administrateur admin) {
        return "ADMIN-" + admin.getExternalIdAdministrateur() + "-" + System.currentTimeMillis();
    }

    /**
     * Déterminer si c'est une première connexion (mot de passe temporaire)
     */
    private boolean isMotDePasseTemporaire(Electeur electeur) {
        // Logique simple : si le mot de passe contient certains patterns
        //  Future : flag en base ou vérification plus sophistiquée
        return true; // Pour l'instant, on assume que c'est toujours temporaire
    }

    /**
     *  Validation nouveau mot de passe
     */
    private void validerNouveauMotDePasse(String motDePasse) {
        if (motDePasse == null || motDePasse.length() < 8) {
            throw new RuntimeException("Le mot de passe doit contenir au moins 8 caractères");
        }

        // Vérification complexité
        if (!motDePasse.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&._-])[A-Za-z\\d@$!%*?&._-]{8,}$")) {
            throw new RuntimeException("Le mot de passe doit contenir une majuscule, une minuscule, un chiffre et un caractère spécial");
        }
    }
}