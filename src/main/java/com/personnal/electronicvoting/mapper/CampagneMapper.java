package com.personnal.electronicvoting.mapper;

import com.personnal.electronicvoting.dto.CampagneDTO;
import com.personnal.electronicvoting.model.Campagne;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CampagneMapper {
    CampagneDTO toDTO (Campagne campagne);
    Campagne toEntity(CampagneDTO dto);
}
