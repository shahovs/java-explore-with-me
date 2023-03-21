package ru.practicum.mainservice.mapper;

import com.querydsl.core.Tuple;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.practicum.mainservice.dto.EventFullDto;
import ru.practicum.mainservice.dto.EventNewDto;
import ru.practicum.mainservice.dto.EventShortDto;
import ru.practicum.mainservice.model.Event;
import ru.practicum.mainservice.model.User;
import ru.practicum.mainservice.service.impl.CategoryServiceAdminImpl;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = "spring",
        uses = {CategoryServiceAdminImpl.class})
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", source = "eventNewDto.category")
    @Mapping(target = "initiator", source = "initiator")
    Event toEntity(EventNewDto eventNewDto, User initiator);

    EventFullDto toEventFullDto(Event event);

    @Mapping(target = "confirmedRequests", source = "confirmedRequests")
    EventFullDto toEventFullDto(Event event, Long confirmedRequests);

    @Mapping(target = "confirmedRequests", source = "confirmedRequests")
    @Mapping(target = "views", source = "hits")
    EventFullDto toEventFullDto(Event event, Long confirmedRequests, Long hits);


//    @Mapping(target = "confirmedRequests", ignore = true)
//    @Mapping(target = "views", ignore = true)
//    EventShortDto toEventShortDto(Event event);

    @Mapping(target = "confirmedRequests", source = "confirmedRequests")
    EventShortDto toEventShortDto(Event event, Long confirmedRequests);

//    @Mapping(target = "confirmedRequests", ignore = true)
//    @Mapping(target = "views", ignore = true)
    List<EventShortDto> toEventShortDto(List<Event> events);

}
