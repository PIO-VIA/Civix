package com.personnal.electronicvoting.dto;

import lombok.*;
import com.personnal.electronicvoting.model.Election;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ElectionDTO {

    private String externalIdElection;
    private String titre;
    private String description;
    private String photo;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateDebut;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateFin;


    private Election.StatutElection statut;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateCreation;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateModification;



    private Boolean autoriserVoteMultiple;
    private Integer nombreMaxVotesParElecteur;
    private Boolean resultatsVisibles;

    private Set<String> electeursAutorises;
    private Set<String> candidatsParticipants;

    private Long nombreElecteursInscrits;
    private Long nombreCandidats;
    private Long nombreVotes;

    private Boolean estActive;
    private Boolean estDansLaPeriodeDeValidite;
}