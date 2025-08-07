package com.personnal.electronicvoting.mapper;

import com.personnal.electronicvoting.dto.CandidatDTO;
import com.personnal.electronicvoting.dto.CandidatSummaryDTO;
import com.personnal.electronicvoting.model.Candidat;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CandidatMapper {
    CandidatDTO toDTO(Candidat candidat);
    Candidat toEntity(CandidatDTO dto);

    @Named("toSummary")
    CandidatSummaryDTO toSummaryDTO(Candidat candidat);

    List<CandidatSummaryDTO> toSummaryList(List<Candidat> candidats);

}
