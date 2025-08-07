package com.personnal.electronicvoting.dto;

import com.personnal.electronicvoting.model.Campagne;
import com.personnal.electronicvoting.model.Vote;
import lombok.*;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidatDTO {
    private String externalIdCandidat;
    private String username;
    private String email;
    private String description;
    private String photo;
    private List<CampagneSummaryDTO> campagnes = new ArrayList<>();


}
