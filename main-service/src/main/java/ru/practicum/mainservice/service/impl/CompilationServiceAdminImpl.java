package ru.practicum.mainservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainservice.dto.CompilationDto;
import ru.practicum.mainservice.dto.CompilationNewDto;
import ru.practicum.mainservice.dto.CompilationUpdateRequestDto;
import ru.practicum.mainservice.dto.EventShortDto;
import ru.practicum.mainservice.exception.ObjectNotFoundException;
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
    private final EventServicePublicImpl eventServicePublic;

    private final CompilationMapper compilationMapper;

    @Transactional
    public CompilationDto saveCompilation(CompilationNewDto compilationNewDto) {
        List<Event> events = eventRepository.findAllById(compilationNewDto.getEvents());
        Compilation compilation = compilationMapper.toEntity(compilationNewDto, events);
        compilationRepository.save(compilation);
        List<EventShortDto> eventShortDtos = eventServicePublic.getEventShortDtos(events, false);
        CompilationDto compilationDto = compilationMapper.toDto(compilation, eventShortDtos);
        return compilationDto;
    }

    @Transactional
    public CompilationDto updateCompilation(Long compId, CompilationUpdateRequestDto compilationUpdateRequestDto) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(
                () -> new ObjectNotFoundException("Подборка не найдена или недоступна"));
        if (compilationUpdateRequestDto.getTitle() != null) {
            compilation.setTitle(compilationUpdateRequestDto.getTitle());
        }
        if (compilationUpdateRequestDto.getPinned() != null) {
            compilation.setPinned(compilationUpdateRequestDto.getPinned());
        }
        if (compilationUpdateRequestDto.getEvents() != null) {
            List<Event> allEventsById = eventRepository.findAllById(compilationUpdateRequestDto.getEvents());
            compilation.setEventsOfCompilation(allEventsById);
        }
        compilationRepository.save(compilation);
        List<EventShortDto> eventShortDtos = eventServicePublic.getEventShortDtos(compilation.getEventsOfCompilation(),
                false);
        CompilationDto compilationDto = compilationMapper.toDto(compilation, eventShortDtos);
        return compilationDto;
    }

    @Transactional
    public void deleteCompilation(Long compId) {
        try {
            compilationRepository.deleteById(compId);
        } catch (EmptyResultDataAccessException e) {
            throw new ObjectNotFoundException("Подборка не найдена или недоступна");
        }
    }

}
