package ru.practicum.mainservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.practicum.mainservice.dto.ParticipationRequestDto;
import ru.practicum.mainservice.model.Event;
import ru.practicum.mainservice.model.ParticipationRequest;
import ru.practicum.mainservice.model.User;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = "spring")
public interface ParticipationRequestMapper {

    @Mapping(target = "id", source = "participationRequestDto.id")
    @Mapping(target = "requester", source = "requester")
    @Mapping(target = "event", source = "event")
    ParticipationRequest toEntity(ParticipationRequestDto participationRequestDto, User requester, Event event);

    @Mapping(target = "requester", source = "requester.id")
    @Mapping(target = "event", source = "event.id")
    ParticipationRequestDto toDto(ParticipationRequest participationRequest);

    @Mapping(target = "requester", source = "requester.id")
    @Mapping(target = "event", source = "event.id")
    List<ParticipationRequestDto> toDtos(List<ParticipationRequest> participationRequest);

}
