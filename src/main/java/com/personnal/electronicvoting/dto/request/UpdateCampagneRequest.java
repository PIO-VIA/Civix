package com.personnal.electronicvoting.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCampagneRequest {

    @Size(max = 5000, message = "La description ne peut pas dépasser 5000 caractères")
    private String description;
    private String photo;
}
