package com.personnal.electronicvoting.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateElecteurRequest {

    private String username;  // Optionnel
    private String email;     // Optionnel
    private boolean resetMotDePasse = false;  // Si true, génère nouveau mot de passe
}
