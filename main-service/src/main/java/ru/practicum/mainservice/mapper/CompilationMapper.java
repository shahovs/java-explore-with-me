package ru.practicum.mainservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.mainservice.dto.CompilationDto;
import ru.practicum.mainservice.dto.CompilationNewDto;
import ru.practicum.mainservice.dto.EventShortDto;
import ru.practicum.mainservice.model.Compilation;
import ru.practicum.mainservice.model.Event;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CompilationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eventsOfCompilation", source = "events")
    Compilation toEntity(CompilationNewDto compilationNewDto, List<Event> events);

    @Mapping(target = "events", source = "eventsOfCompilation")
    CompilationDto toDto(Compilation compilation);

    @Mapping(target = "events", source = "events")
    CompilationDto toDto(Compilation compilation, List<EventShortDto> events);

//    @Mapping(target = "events", source = "eventsOfCompilation")
    List<CompilationDto> toDtos(List<Compilation> compilations);

//    @Mapping(target = "events", source = "events")
//    List<CompilationDto> toDtos(List<Compilation> compilations, List<List<EventShortDto>> events);

}
