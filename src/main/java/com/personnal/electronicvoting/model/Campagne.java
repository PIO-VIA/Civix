package com.personnal.electronicvoting.model;
import lombok.*;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Campagne {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_campagne")
    private long IdCampagne;

    @NotBlank
    @Column(name = "external_id_campagne", unique = true, nullable = false, updatable = false)
    private String ExternalIdCampagne;

    @NotBlank(message = "La description est obligatoire")
    @Column(length = 5000)
    private String description;

    @Column(name = "photo_path", nullable = true)
    private String photo;

    @ManyToOne
    @JoinColumn(name = "candidat_id", nullable = false)
    private Candidat candidat;

    @PrePersist
    public void generateExternalId() {
        if (this.ExternalIdCampagne == null) {
            this.ExternalIdCampagne = UUID.randomUUID().toString();
        }
    }

}
