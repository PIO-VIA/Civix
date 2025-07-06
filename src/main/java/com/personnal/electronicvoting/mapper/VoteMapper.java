package com.personnal.electronicvoting.mapper;

import com.personnal.electronicvoting.dto.VoteDTO;
import com.personnal.electronicvoting.model.Vote;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VoteMapper {

    @Mapping(source = "electeur.externalIdElecteur", target = "electeurId")
    @Mapping(source = "candidat.externalIdCandidat", target = "candidatId")
    VoteDTO toDTO(Vote vote);

    @Mapping(target = "electeur", ignore = true)
    @Mapping(target = "candidat", ignore = true)
    Vote toEntity(VoteDTO dto);
}
