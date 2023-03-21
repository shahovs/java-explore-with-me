package ru.practicum.mainservice.service.impl;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainservice.dto.CompilationDto;
import ru.practicum.mainservice.dto.EventShortDto;
import ru.practicum.mainservice.exception.ObjectNotFoundException;
import ru.practicum.mainservice.mapper.CompilationMapper;
import ru.practicum.mainservice.mapper.EventMapper;
import ru.practicum.mainservice.model.*;
import ru.practicum.mainservice.repository.CompilationRepository;

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
public class CompilationServicePublicImpl {

    private final CompilationRepository compilationRepository;
    private final EventServicePublicImpl eventServicePublic;
    private final CompilationMapper compilationMapper;
    private final EventMapper eventMapper;
    private final EntityManager entityManager;

    public List<CompilationDto> getAllCompilations(Boolean pinned, Integer fromElement, Integer size) {
        int fromPage = fromElement / size;
        Pageable pageable = PageRequest.of(fromPage, size);
        List<Compilation> allByPinned = compilationRepository.findAllByPinned(pinned, pageable);
        List<CompilationDto> resultList = new ArrayList<>();
        for (Compilation compilation : allByPinned) {
            CompilationDto compilationDto;
            if (compilation.getEventsOfCompilation() == null || compilation.getEventsOfCompilation().size() == 0) {
                compilationDto = compilationMapper.toDto(compilation);
            } else {
                List<EventShortDto> eventShortDtos = toEventShortDtos(compilation.getEventsOfCompilation());
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
        List<EventShortDto> eventShortDtos = toEventShortDtos(events);
        CompilationDto compilationDto = compilationMapper.toDto(compilation, eventShortDtos);
        return compilationDto;
    }

    private List<EventShortDto> toEventShortDtos(List<Event> events) {
        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .collect(Collectors.toList());

        // подготавливаем переменные для формирования запроса
        QEvent qEvent = QEvent.event;
        QParticipationRequest qParticipationRequest = QParticipationRequest.participationRequest;
        JPAQueryFactory factory = new JPAQueryFactory(entityManager);

        // основная часть запроса (select, from, join, where)
        JPAQuery<Tuple> query = factory
                .select(qEvent.id, qParticipationRequest.count())
                .from(qEvent)
                .leftJoin(qParticipationRequest).on(qParticipationRequest.event.eq(qEvent))
                .where(qEvent.id.in(eventIds))
                .where(qParticipationRequest.status.eq(ParticipationRequestStatus.CONFIRMED)
                        .or(qParticipationRequest.isNull()));
        // группируем (по событиям - у одного события может быть несколько строк - по кол-ву запросов на участие)
        query.groupBy(qEvent, qEvent.category.name, qEvent.initiator.name);
        // делаем запрос
        List<Tuple> tuples = query.fetch();

        // создаем список dto и мапу
        List<EventShortDto> eventDtos = eventMapper.toEventShortDto(events);
        Map<Long, EventShortDto> eventDtosByIds = eventDtos.stream()
                .collect(Collectors.toMap(EventShortDto::getId, Function.identity()));

        // добавляем в каждое dto количество запросов на участие
        for (Tuple tuple : tuples) {
            Long id = tuple.get(qEvent.id);
            Long requestsCount = tuple.get(qParticipationRequest.count());
            EventShortDto eventShortDto = eventDtosByIds.get(id);
            eventShortDto.setConfirmedRequests(requestsCount);
        }

        // запрашиваем просмотры в сервисе статистики (будут получены только те события, у которые были просмотры)
        Map<Long, Long> viewsMap = eventServicePublic.getViews(eventIds);

        // добавляем просмотры в dto
        for (EventShortDto dto : eventDtos) {
            Long views = viewsMap.get(dto.getId());
            dto.setViews(Objects.requireNonNullElse(views, 0L));
        }
        return eventDtos;
    }

}
