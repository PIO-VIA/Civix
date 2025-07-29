package com.personnal.electronicvoting.dto.request;

import lombok.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateElectionRequest {

    @NotBlank(message = "Le titre de l'élection est obligatoire")
    @Size(max = 200, message = "Le titre ne peut pas dépasser 200 caractères")
    private String titre;

    @Size(max = 5000, message = "La description ne peut pas dépasser 5000 caractères")
    private String description;

    private String photo;

    @NotNull(message = "La date de début est obligatoire")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateDebut;

    @NotNull(message = "La date de fin est obligatoire")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateFin;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateDebutValidite;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateFinValidite;

    private Boolean autoriserVoteMultiple = false;

    @Min(value = 1, message = "Le nombre maximum de votes par électeur doit être au moins 1")
    private Integer nombreMaxVotesParElecteur = 1;

    private Boolean resultatsVisibles = false;

    private Set<String> electeursAutorises;
    private Set<String> candidatsParticipants;
}