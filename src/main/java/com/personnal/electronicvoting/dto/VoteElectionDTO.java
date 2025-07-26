package com.personnal.electronicvoting.dto;

import lombok.*;
import com.personnal.electronicvoting.model.VoteElection;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoteElectionDTO {

    private Long id;
    private String electionId;
    private String electeurId;
    private String candidatId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateVote;

    private String adresseIp;
    private String userAgent;
    private VoteElection.StatutVote statutVote;

    private String electionTitre;
    private String electeurNom;
    private String candidatNom;
}