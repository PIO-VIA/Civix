package com.personnal.electronicvoting.dto.request;

import lombok.*;
import jakarta.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoterElectionRequest {

    @NotBlank(message = "L'ID de l'élection est obligatoire")
    private String electionId;

    @NotBlank(message = "L'ID du candidat est obligatoire")
    private String candidatId;

    private String adresseIp;
    private String userAgent;
}