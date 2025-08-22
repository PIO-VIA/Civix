package com.personnal.electronicvoting.dto.request;

import lombok.*;
import jakarta.validation.constraints.*;
import com.personnal.electronicvoting.model.Election;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
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

    private String photo;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateDebut;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateFin;


    private Election.StatutElection statut;

    private Boolean autoriserVoteMultiple;

    @Min(value = 1, message = "Le nombre maximum de votes par électeur doit être au moins 1")
    private Integer nombreMaxVotesParElecteur;

    private Boolean resultatsVisibles;

    private Set<String> electeursAutorises;
    private Set<String> candidatsParticipants;
}