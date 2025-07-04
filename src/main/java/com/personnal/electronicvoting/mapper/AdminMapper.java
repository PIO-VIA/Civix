package com.personnal.electronicvoting.mapper;

import com.personnal.electronicvoting.dto.AdministrateurDTO;
import com.personnal.electronicvoting.model.Administrateur;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AdminMapper {
    AdministrateurDTO toDTO(Administrateur admin);
    Administrateur toEntity(AdministrateurDTO dto);
}
