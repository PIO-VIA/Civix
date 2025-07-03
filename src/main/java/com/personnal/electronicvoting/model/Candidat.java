package com.personnal.electronicvoting.model;
import lombok.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.*;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Candidat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id_candidat", unique = true, nullable = false, updatable = false)
    private String externalIdCandidat;

    @NotBlank
    private String nom;

    @NotBlank
    private int count;

    @OneToMany(mappedBy = "candidat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Campagne> campagnes = new ArrayList<>();

    @PrePersist
    public void generateExternalId() {
        if (this.externalIdCandidat == null) {
            this.externalIdCandidat = UUID.randomUUID().toString();
        }
    }
}
