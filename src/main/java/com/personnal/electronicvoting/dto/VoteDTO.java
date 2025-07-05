package com.personnal.electronicvoting.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoteDTO {

    private String electeurId;     // externalId de l'électeur
    private String candidatId;     // externalId du candidat
    private LocalDateTime dateVote;
}
