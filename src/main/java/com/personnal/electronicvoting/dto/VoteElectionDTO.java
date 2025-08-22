package com.personnal.electronicvoting.dto;

import lombok.*;
import com.personnal.electronicvoting.model.VoteElection;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoteElectionDTO {

    private Long id;
    private String electionId;
    private String electeurId;
    private String candidatId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateVote;

    private String adresseIp;
    private String userAgent;
    private VoteElection.StatutVote statutVote;

    private String electionTitre;
    private String electeurNom;
    private String candidatNom;
}