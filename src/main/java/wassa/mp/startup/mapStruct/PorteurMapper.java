package wassa.mp.startup.mapStruct;

import org.mapstruct.Mapper;
import wassa.mp.startup.dto.PorteurRequestDto;
import wassa.mp.startup.dto.PorteurResponseDto;
import wassa.mp.startup.model.Porteur;

@Mapper(componentModel="spring")
public interface PorteurMapper {
    //cette methode est appelée si on veut retourner objet dto
    PorteurResponseDto toDTO(Porteur porteur);
    Porteur toEntity(PorteurRequestDto porteurRequestDto);
}
