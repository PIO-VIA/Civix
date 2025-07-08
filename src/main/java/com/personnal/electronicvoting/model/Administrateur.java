package com.personnal.electronicvoting.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;
import lombok.*;



@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Administrateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_administrateur")
    private long IdAdmin;

    @Column(name = "external_id_administrateur", unique = true, nullable = false, updatable = false)
    private String externalIdAdministrateur;

    @NotBlank(message = "Le nom ne peut pas être vide")
    @Column(name = "nom_administrateur", nullable = false)
    private String username;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&._-])[A-Za-z\\d@$!%*?&_.-]{8,}$",
            message = "Le mot de passe doit contenir une majuscule, une minuscule, un chiffre et un caractère spécial"
    )
    @JsonIgnore
    @Column(name = "mot_de_passe", nullable = false)
    private String motDePasse;

    @NotBlank
    @Email
    private String email;


    @Column(name = "empreinte_digitale", columnDefinition = "BYTEA")
    private byte[] empreinteDigitale;

    @PrePersist
    public void generateExternalId() {
        if (this.externalIdAdministrateur == null) {
            this.externalIdAdministrateur = UUID.randomUUID().toString();
        }
    }


}
