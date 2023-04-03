package ru.practicum.mainservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainservice.dto.CompilationDto;
import ru.practicum.mainservice.dto.EventShortDto;
import ru.practicum.mainservice.exception.ObjectNotFoundException;
import ru.practicum.mainservice.mapper.CompilationMapper;
import ru.practicum.mainservice.model.Compilation;
import ru.practicum.mainservice.model.Event;
import ru.practicum.mainservice.repository.CompilationRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServicePublicImpl {

    private final CompilationRepository compilationRepository;
    private final EventServicePublicImpl eventServicePublic;
    private final CompilationMapper compilationMapper;

    public List<CompilationDto> getAllCompilations(Boolean pinned, Integer fromElement, Integer size) {
        int fromPage = fromElement / size;
        Pageable pageable = PageRequest.of(fromPage, size);
        // Запрашиваем подборки + Event + User + Category (запросы на участие в событиях сразу не запрашиваем)
        List<Compilation> compilations = compilationRepository.findAllByPinned(pinned, pageable);

        List<CompilationDto> resultList = new ArrayList<>();
        for (Compilation compilation : compilations) {
            CompilationDto compilationDto;
            // если у подборки нет ни одного события
            if (compilation.getEventsOfCompilation() == null || compilation.getEventsOfCompilation().size() == 0) {
                compilationDto = compilationMapper.toDto(compilation);
            } else {
                List<EventShortDto> eventShortDtos = eventServicePublic.mapToEventShortDtos(
                        compilation.getEventsOfCompilation());
                compilationDto = compilationMapper.toDto(compilation, eventShortDtos);
            }
            resultList.add(compilationDto);
        }

        return resultList;
    }

    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(
                () -> new ObjectNotFoundException("Подборка не найдена или недоступна"));
        List<Event> events = compilation.getEventsOfCompilation();
        List<EventShortDto> eventShortDtos = eventServicePublic.mapToEventShortDtos(events);
        CompilationDto compilationDto = compilationMapper.toDto(compilation, eventShortDtos);
        return compilationDto;
    }

}
