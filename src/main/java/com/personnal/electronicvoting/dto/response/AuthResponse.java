package com.personnal.electronicvoting.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String userId;              // ExternalId de l'utilisateur
    private String username;            // Nom d'utilisateur
    private String email;               // Email
    private String role;                // "ADMIN" ou "ELECTEUR"
    private String token;               // Token de session (simplifié)
    private Boolean aVote;              // Null pour admin, true/false pour électeur
    private boolean premierConnexion;   // Si première connexion (changement mot de passe requis)


    @Builder.Default
    private long connectionTime = System.currentTimeMillis();
}