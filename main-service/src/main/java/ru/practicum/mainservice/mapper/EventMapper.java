package ru.practicum.mainservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.practicum.mainservice.dto.EventFullDto;
import ru.practicum.mainservice.dto.EventNewDto;
import ru.practicum.mainservice.model.Event;
import ru.practicum.mainservice.model.User;
import ru.practicum.mainservice.service.impl.CategoryServiceAdminImpl;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = "spring",
        uses = {CategoryServiceAdminImpl.class})
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", source = "eventNewDto.category")
    @Mapping(target = "initiator", source = "initiator")
    Event toEntity(EventNewDto eventNewDto, User initiator);

    EventFullDto toEventFullDto(Event event);

}
