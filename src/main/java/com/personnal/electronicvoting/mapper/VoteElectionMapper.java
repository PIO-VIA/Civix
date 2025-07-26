package com.personnal.electronicvoting.mapper;

import com.personnal.electronicvoting.dto.VoteElectionDTO;
import com.personnal.electronicvoting.model.VoteElection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VoteElectionMapper {

    @Mapping(target = "electionId", source = "election.externalIdElection")
    @Mapping(target = "electeurId", source = "electeur.externalIdElecteur")
    @Mapping(target = "candidatId", source = "candidat.externalIdCandidat")
    @Mapping(target = "electionTitre", source = "election.titre")
    @Mapping(target = "electeurNom", source = "electeur.username")
    @Mapping(target = "candidatNom", source = "candidat.username")
    VoteElectionDTO toDTO(VoteElection voteElection);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "election", ignore = true)
    @Mapping(target = "electeur", ignore = true)
    @Mapping(target = "candidat", ignore = true)
    @Mapping(target = "dateVote", ignore = true)
    VoteElection toEntity(VoteElectionDTO dto);
}