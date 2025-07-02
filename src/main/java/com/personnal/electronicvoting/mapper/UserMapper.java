package com.personnal.electronicvoting.mapper;

import com.personnal.electronicvoting.dto.ElecteurDTO;
import com.personnal.electronicvoting.model.Electeur;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    ElecteurDTO toDTO(Electeur user);
    Electeur toEntity(ElecteurDTO dto);

}
