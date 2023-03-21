package ru.practicum.mainservice.service.impl;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainservice.dto.*;
import ru.practicum.mainservice.exception.ObjectNotFoundException;
import ru.practicum.mainservice.mapper.CompilationMapper;
import ru.practicum.mainservice.mapper.EventMapper;
import ru.practicum.mainservice.model.*;
import ru.practicum.mainservice.repository.CompilationRepository;
import ru.practicum.mainservice.repository.EventRepository;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceAdminImpl {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationServicePublicImpl compilationServicePublic;
    private final EventServicePublicImpl eventServicePublic;

    private final CompilationMapper compilationMapper;
    private final EventMapper eventMapper;
    private final EntityManager entityManager;

    @Transactional
    public CompilationDto saveCompilation(CompilationNewDto compilationNewDto) {
        // сначала получаем из репозитория обертки (Event + кол-во Long confirmedRequests)
        List<Long> eventIds = compilationNewDto.getEvents();
        List<EventWithConfirmedRequestsDto> wrappers = eventRepository.findEvents(eventIds);
        // извлекаем Events
        List<Event> events = wrappers.stream()
                .map(EventWithConfirmedRequestsDto::getEvent)
                .collect(Collectors.toList());

        // создаем и сохраняем в репозиторий новую подборку событий
        Compilation compilation = compilationMapper.toEntity(compilationNewDto, events);
        Compilation savedCompilation = compilationRepository.save(compilation);

        // преобразуем обертки в dtos (добавляя просмотры из сервиса статистики)
        List<EventShortDto> eventShortDtos = toEventShortDtos(wrappers);

        // преобразуем данные в возвращаемый результат (dto)
//        List<EventShortDto> eventShortDtos = wrappers.stream()
//                .map(wrapper -> eventMapper.toEventShortDto(wrapper.getEvent(), wrapper.getConfirmedRequests()))
//                .collect(Collectors.toList());
        CompilationDto compilationDto = compilationMapper.toDto(savedCompilation, eventShortDtos);
        return compilationDto;
    }

    private List<EventShortDto> toEventShortDtos(List<EventWithConfirmedRequestsDto> wrappers) {
        List<EventShortDto> eventDtos = new ArrayList<>();
        for (EventWithConfirmedRequestsDto wrapper : wrappers) {
            EventShortDto eventShortDto =
                    eventMapper.toEventShortDto(wrapper.getEvent(), wrapper.getConfirmedRequests());
            eventDtos.add(eventShortDto);
        }

        List<Long> eventIds = eventDtos.stream()
                .map(EventShortDto::getId)
                .collect(Collectors.toList());

        Map<Long, EventShortDto> eventDtosByIds = eventDtos.stream()
                .collect(Collectors.toMap(EventShortDto::getId, Function.identity()));

        // запрашиваем просмотры в сервисе статистики (будут получены только те события, у которые были просмотры)
        Map<Long, Long> viewsMap = eventServicePublic.getViews(eventIds);

        // добавляем просмотры в dto
        for (EventShortDto dto : eventDtos) {
            Long views = viewsMap.get(dto.getId());
            dto.setViews(Objects.requireNonNullElse(views, 0L));
        }
        return eventDtos;
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
        Compilation savedCompilation = compilationRepository.save(compilation);

        // обновление выполнено; готовим возвращаемый объект
        List<Long> eventsIds = savedCompilation.getEventsOfCompilation().stream()
                .map(Event::getId)
                .collect(Collectors.toList());
        List<EventWithConfirmedRequestsDto> wrappers = eventRepository.findEvents(eventsIds);
//        List<EventShortDto> eventShortDtos = wrappers.stream()
//                .map(wrapper -> eventMapper.toEventShortDto(wrapper.getEvent(), wrapper.getConfirmedRequests()))
//                .collect(Collectors.toList());
        List<EventShortDto> eventShortDtos = toEventShortDtos(wrappers);
        return compilationMapper.toDto(savedCompilation, eventShortDtos);
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
