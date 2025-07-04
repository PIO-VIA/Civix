package com.personnal.electronicvoting.dto;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdministrateurDTO {
    private String externalIdElecteur;
    private String username;
    private String motDePasse;
    private String email;
    private byte[] empreinteDigitale;

}
