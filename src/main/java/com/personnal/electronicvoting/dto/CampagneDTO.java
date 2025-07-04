package com.personnal.electronicvoting.dto;

import com.personnal.electronicvoting.model.Candidat;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampagneDTO {
    private String ExternalIdCampagne;
    private String description;
    private String photo;
    private Candidat candidat;

}
