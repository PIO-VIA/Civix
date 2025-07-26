package com.personnal.electronicvoting.dto.request;

import lombok.*;
import jakarta.validation.constraints.*;
import com.personnal.electronicvoting.model.Election;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateElectionRequest {

    @Size(max = 200, message = "Le titre ne peut pas dépasser 200 caractères")
    private String titre;

    @Size(max = 5000, message = "La description ne peut pas dépasser 5000 caractères")
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateDebut;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateFin;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateDebutValidite;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateFinValidite;

    private Election.StatutElection statut;

    private Boolean autoriserVoteMultiple;

    @Min(value = 1, message = "Le nombre maximum de votes par électeur doit être au moins 1")
    private Integer nombreMaxVotesParElecteur;

    private Boolean resultatsVisibles;

    private Set<String> electeursAutorises;
    private Set<String> candidatsParticipants;
}