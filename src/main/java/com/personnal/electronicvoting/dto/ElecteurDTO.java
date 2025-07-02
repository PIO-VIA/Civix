package com.personnal.electronicvoting.dto;

import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ElecteurDTO {
    private String externalIdElecteur;
    private String username;
    private String email;
    private byte[] empreinteDigitale;
    private boolean aVote;

}
