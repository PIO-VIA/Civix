package com.personnal.electronicvoting.mapper;

import com.personnal.electronicvoting.dto.CampagneDTO;
import com.personnal.electronicvoting.dto.CampagneSummaryDTO;
import com.personnal.electronicvoting.model.Campagne;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CampagneMapper {
    CampagneDTO toDTO (Campagne campagne);
    Campagne toEntity(CampagneDTO dto);

    @Named("toSummary")
    CampagneSummaryDTO toSummaryDTO(Campagne campagne);

    List<CampagneSummaryDTO> toSummaryList(List<Campagne> campagnes);

}
