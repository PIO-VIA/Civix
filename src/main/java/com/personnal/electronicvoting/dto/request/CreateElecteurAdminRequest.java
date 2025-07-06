package com.personnal.electronicvoting.dto.request;

import lombok.*;
import jakarta.validation.constraints.*;

/**
 * 👥 DTO pour qu'un admin crée un électeur
 * L'admin ne saisit PAS le mot de passe (généré automatiquement)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateElecteurAdminRequest {

    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    @Size(min = 3, max = 50, message = "Le nom d'utilisateur doit contenir entre 3 et 50 caractères")
    private String username;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Le format de l'email est invalide")
    private String email;

    // ❌ Pas de mot de passe ! Généré automatiquement et envoyé par email

    private byte[] empreinteDigitale;  // Optionnel pour l'instant
}
