package com.personnal.electronicvoting.model;
import lombok.*;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import java.util.UUID;


@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"motDePasse"})
@AllArgsConstructor
@NoArgsConstructor
public class Electeur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_electeur")
    private long IdElecteur;

    @Column(name = "external_id_electeur", unique = true, nullable = false, updatable = false)
    private String externalIdElecteur;

    @NotBlank(message = "Le nom ne peut pas être vide")
    @Column(name = "nom_electeur", nullable = false)
    private String username;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
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

    @Column(name="a_vote?" ,nullable=false)
    private boolean aVote = false;

    @PrePersist
    public void generateExternalId() {
        if (this.externalIdElecteur == null) {
            this.externalIdElecteur = UUID.randomUUID().toString();
        }
    }



}
