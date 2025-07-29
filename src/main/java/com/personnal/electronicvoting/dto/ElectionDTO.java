package com.personnal.electronicvoting.dto;

import lombok.*;
import com.personnal.electronicvoting.model.Election;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
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

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateDebut;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateFin;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateDebutValidite;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateFinValidite;

    private Election.StatutElection statut;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateCreation;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateModification;

    private String administrateurId;
    private String administrateurNom;

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