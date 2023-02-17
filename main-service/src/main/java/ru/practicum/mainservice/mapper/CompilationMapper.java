package ru.practicum.mainservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.mainservice.dto.CompilationDto;
import ru.practicum.mainservice.dto.CompilationNewDto;
import ru.practicum.mainservice.model.Compilation;
import ru.practicum.mainservice.model.Event;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CompilationMapper {

    //    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eventsOfCompilation", source = "events")
    Compilation toEntity(CompilationNewDto compilationNewDto, List<Event> events);

    @Mapping(target = "events", source = "eventsOfCompilation")
    CompilationDto toDto(Compilation compilation);

}
