package com.personnal.electronicvoting.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCandidatRequest {

    private String username;  // Optionnel
    private String description;  // Optionnel
    private String photo;  // Optionnel
}