package com.personnal.electronicvoting.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampagneSummaryDTO {
    private String externalIdCampagne;
    private String description;
    private String photo;
}
