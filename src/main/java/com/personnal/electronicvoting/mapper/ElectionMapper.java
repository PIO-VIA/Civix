package com.personnal.electronicvoting.mapper;

import com.personnal.electronicvoting.dto.ElectionDTO;
import com.personnal.electronicvoting.model.Election;
import com.personnal.electronicvoting.model.Electeur;
import com.personnal.electronicvoting.model.Candidat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ElectionMapper {


    @Mapping(target = "electeursAutorises", source = "electeursAutorises", qualifiedByName = "extractElecteurIds")
    @Mapping(target = "candidatsParticipants", source = "candidats", qualifiedByName = "extractCandidatIds")
    @Mapping(target = "nombreElecteursInscrits", expression = "java(election.getElecteursAutorises() != null ? (long) election.getElecteursAutorises().size() : 0L)")
    @Mapping(target = "nombreCandidats", expression = "java(election.getCandidats() != null ? (long) election.getCandidats().size() : 0L)")
    @Mapping(target = "nombreVotes", expression = "java(election.getVotes() != null ? (long) election.getVotes().size() : 0L)")
    @Mapping(target = "estActive", expression = "java(election.estActive())")
    ElectionDTO toDTO(Election election);

    @Mapping(target = "idElection", ignore = true)
    @Mapping(target = "externalIdElection", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "dateModification", ignore = true)
    @Mapping(target = "administrateur", ignore = true)
    @Mapping(target = "electeursAutorises", ignore = true)
    @Mapping(target = "candidats", ignore = true)
    @Mapping(target = "votes", ignore = true)
    Election toEntity(ElectionDTO dto);

    @Named("extractElecteurIds")
    default Set<String> extractElecteurIds(Set<Electeur> electeurs) {
        if (electeurs == null) {
            return null;
        }
        return electeurs.stream()
                .map(Electeur::getExternalIdElecteur)
                .collect(Collectors.toSet());
    }

    @Named("extractCandidatIds")
    default Set<String> extractCandidatIds(Set<Candidat> candidats) {
        if (candidats == null) {
            return null;
        }
        return candidats.stream()
                .map(Candidat::getExternalIdCandidat)
                .collect(Collectors.toSet());
    }
}