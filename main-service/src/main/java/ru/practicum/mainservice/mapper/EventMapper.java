package ru.practicum.mainservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.practicum.mainservice.dto.EventFullDto;
import ru.practicum.mainservice.dto.EventNewDto;
import ru.practicum.mainservice.dto.EventShortDto;
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
    // Категория берется с помощью метода в классе CategoryServiceAdminImpl, указанном в аннтотации к этому классу
    // В остальных случаях необходимые сущности передаются вторым, третьим и т.д. аргументами - например, как с User)
    Event toEntity(EventNewDto eventNewDto, User initiator);

    EventFullDto toEventFullDto(Event event);

    @Mapping(target = "confirmedRequests", source = "confirmedRequests")
    @Mapping(target = "views", source = "views")
    EventFullDto toEventFullDto(Event event, Long confirmedRequests, Long views);

    @Mapping(target = "confirmedRequests", source = "confirmedRequests")
    @Mapping(target = "views", source = "views")
    EventShortDto toEventShortDto(Event event, Long confirmedRequests, Long views);

}
