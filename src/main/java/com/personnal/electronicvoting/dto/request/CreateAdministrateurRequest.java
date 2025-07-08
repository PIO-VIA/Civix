package com.personnal.electronicvoting.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAdministrateurRequest {

    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    private String username;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Minimum 8 caractères")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&_.-])[A-Za-z\\d@$!%*?&_.-]{8,}$",
            message = "Le mot de passe doit contenir une majuscule, minuscule, chiffre et caractère spécial"
    )
    private String motDePasse;

    private byte[] empreinteDigitale;
}