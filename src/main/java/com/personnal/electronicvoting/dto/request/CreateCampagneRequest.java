package com.personnal.electronicvoting.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCampagneRequest {

    @NotBlank(message = "L'ID du candidat est obligatoire")
    private String candidatId;

    @NotBlank(message = "La description est obligatoire")
    @Size(max = 5000, message = "La description ne peut pas dépasser 5000 caractères")
    private String description;

    private String photo;  // URL ou chemin vers l'image
}