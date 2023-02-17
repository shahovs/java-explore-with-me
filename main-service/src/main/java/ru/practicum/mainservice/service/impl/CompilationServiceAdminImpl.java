package ru.practicum.mainservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainservice.dto.CompilationDto;
import ru.practicum.mainservice.dto.CompilationNewDto;
import ru.practicum.mainservice.mapper.CompilationMapper;
import ru.practicum.mainservice.model.Compilation;
import ru.practicum.mainservice.model.Event;
import ru.practicum.mainservice.repository.CompilationRepository;
import ru.practicum.mainservice.repository.EventRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceAdminImpl {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;

    @Transactional
    public CompilationDto saveCompilation(CompilationNewDto compilationNewDto) {
        List<Event> allEventsById = eventRepository.findAllById(compilationNewDto.getEvents());
        // на самом деле нужен метод, который возвращает из репозитория EventsWithViewsAndConfirmedRequests
        // EventWithViewsAndConfirmedRequests - класс с тремя полями
        // (List<Event> events, HashMap<Long eventId, ViewsAndConfirmedRequests>)
        Compilation compilation = compilationMapper.toEntity(compilationNewDto, allEventsById);
        Compilation savedCompilation = compilationRepository.save(compilation);
        System.out.println("\n\nsavedCompilation: " + savedCompilation);
        CompilationDto compilationDto = compilationMapper.toDto(savedCompilation);
        System.out.println("\n\ncompilationDto: " + compilationDto);
        return compilationDto;
    }

}
