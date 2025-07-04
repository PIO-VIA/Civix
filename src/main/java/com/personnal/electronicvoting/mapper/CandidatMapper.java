package com.personnal.electronicvoting.mapper;

import com.personnal.electronicvoting.dto.CandidatDTO;
import com.personnal.electronicvoting.model.Candidat;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CandidatMapper {
    CandidatDTO toDTO(Candidat candidat);
    Candidat toEntity(CandidatDTO dto);
}
