package ru.practicum.mainservice.service.impl;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stat.client.StatsClient;
import ru.practicum.ewm.stat.dto.HitShortWithHitsDtoResponse;
import ru.practicum.mainservice.dto.EventFullDto;
import ru.practicum.mainservice.dto.EventShortDto;
import ru.practicum.mainservice.exception.ObjectNotFoundException;
import ru.practicum.mainservice.exception.ValidateException;
import ru.practicum.mainservice.mapper.EventMapper;
import ru.practicum.mainservice.model.*;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServicePublicImpl {

    private final EventMapper eventMapper;
    private final StatsClient statsClient;
    private final EntityManager entityManager;

    public EventFullDto getEventById(Long eventId, String ip) {
        EntityGraph<?> entityGraph = entityManager.getEntityGraph("event-entity-graph");
        Map<String, Object> properties = new HashMap<>();
        properties.put("javax.persistence.fetchgraph", entityGraph);
        Event event = entityManager.find(Event.class, eventId, properties);
        validateEvent(event);
        Long confirmedRequests = event.getRequests().stream()
                .filter(request -> Objects.equals(request.getStatus(), ParticipationRequestStatus.CONFIRMED))
                .count();
        // получаем количество просмотров события (из статистики)
        Long views = getViewsOfOneEvent(eventId);
        // отправляем в статистику информацию о просмотре события
        statsClient.postStatMonolith("ewm-main-service", "/events/" + eventId, ip, LocalDateTime.now());
        EventFullDto eventFullDto = eventMapper.toEventFullDto(event, confirmedRequests, views);
        return eventFullDto;
    }

    private static void validateEvent(Event event) {
        if (event == null) {
            throw new ObjectNotFoundException("Событие не найдено");
        }
        if (!Objects.equals(event.getState(), EventState.PUBLISHED)) {
            throw new ValidateException("Событие должно быть опубликовано");
        }
    }

    // запрашиваем в статистике количество просмотров
    // метод не private, поскольку используется также классом EventServicePrivateImpl
    Long getViewsOfOneEvent(Long eventId) {
        // подготавливаем данные
        String[] uri = {"/events/" + eventId};
        LocalDateTime start = LocalDateTime.of(2023, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.now();

        // запрашиваем статистику
        HitShortWithHitsDtoResponse[] statArray = statsClient.getStatArray(start, end, uri, false);
        if (statArray.length == 1) {
            HitShortWithHitsDtoResponse hitShortWithHitsDtoResponse = statArray[0];
            return hitShortWithHitsDtoResponse.getHits();
        }
        return 0L;
    }

    public List<EventShortDto> getPublishedEvents(String text, List<Long> categories, Boolean paid,
                                                  LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                  Boolean onlyAvailable,
                                                  String sort, int from, int size, String ip) {
        // подготавливаем переменные для формирования запроса
        QEvent qEvent = QEvent.event;
        JPAQueryFactory factory = new JPAQueryFactory(entityManager);
        // основная часть запроса (select, where == PUBLISHED)
        JPAQuery<Event> query = factory.selectFrom(qEvent)
                .where(qEvent.state.eq(EventState.PUBLISHED));

        // добавляем условия, полученные в запросе (даты, текст, категории, платность участия)
        addDates(rangeStart, rangeEnd, query, qEvent);
        addTextCategoriesAndPaid(text, categories, paid, query, qEvent);

        if (sort != null && sort.equals("EVENT_DATE")) {
            query.orderBy(qEvent.eventDate.asc());
        }

        // применяем from, size
        query.offset(from).limit(size);

        // просим добавить к запросу сущности User + Category + List PartRequests (тогда у нас будет 1 запрос вместо 4)
        EntityGraph<?> entityGraph = entityManager.getEntityGraph("event-entity-graph");
        query.setHint("javax.persistence.fetchgraph", entityGraph);

        // делаем запрос
        List<Event> events = query.fetch();

        List<EventShortDto> eventDtos = mapToEventShortDtos(events, onlyAvailable);

        if (sort != null && sort.equals("VIEWS")) {
            eventDtos.sort(Comparator.comparing(EventShortDto::getViews).reversed());
        }

        // отправляем в статистику данные о сделанном запросе
        statsClient.postStat("ewm-main-service", "/events", ip, LocalDateTime.now());
        return eventDtos;
    }

    // метод не private, поскольку используется классом EventServicePrivateImpl и классами подборок
    List<EventShortDto> mapToEventShortDtos(List<Event> events) {
        return mapToEventShortDtos(events, false);
    }

    private List<EventShortDto> mapToEventShortDtos(List<Event> events, Boolean onlyAvailable) {
        // запрашиваем просмотры в сервисе статистики (будут получены только те события, у которых были просмотры)
        Map<Long, Long> viewsMap = getViewsMap(events);
        List<EventShortDto> eventDtos = new ArrayList<>();
        for (Event event : events) {
            long views = viewsMap.getOrDefault(event.getId(), 0L);
            int participantLimit = event.getParticipantLimit();
            long confirmedRequests = getConfirmedRequests(event, participantLimit);
            // если onlyAvailable == false, то добавляем все события (!onlyAvailable),
            // иначе добавляем только те события, у которых нет лимита участия (participantLimit == 0),
            // или у которых не исчерпан лимит участия (participantLimit - confirmedRequests) > 0)
            if (!onlyAvailable || (participantLimit == 0) || ((participantLimit - confirmedRequests) > 0)) {
                eventDtos.add(eventMapper.toEventShortDto(event, confirmedRequests, views));
            }
        }
        return eventDtos;
    }

    static long getConfirmedRequests(Event event, int participantLimit) {
        long confirmedRequests = 0;
        if (participantLimit != 0) {
            List<ParticipationRequest> requests = event.getRequests();
            confirmedRequests = requests.stream()
                    .filter(request -> Objects.equals(request.getStatus(), ParticipationRequestStatus.CONFIRMED))
                    .count();
        }
        return confirmedRequests;
    }

    Map<Long, Long> getViewsMap(List<Event> events) {
        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .collect(Collectors.toList());
        Map<Long, Long> viewsMap = getViewsByIds(eventIds);
        return viewsMap;
    }

    private Map<Long, Long> getViewsByIds(List<Long> eventIds) {
        if (eventIds == null || eventIds.size() == 0) {
            return null;
        }
        // подготавливаем данные
        String[] uri = new String[eventIds.size()];
        final String EVENTS = "/events/";
        for (int i = 0; i < eventIds.size(); i++) {
            uri[i] = EVENTS + eventIds.get(i);
        }
        LocalDateTime start = LocalDateTime.of(2023, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.now();

        // запрашиваем статистику
        List<HitShortWithHitsDtoResponse> statArray = statsClient.getStatList(start, end, uri, false);

        // обрабатываем результат запроса
        int indexOfStartOfId = EVENTS.length();
        Map<Long, Long> result = new HashMap<>();
        for (HitShortWithHitsDtoResponse hit : statArray) {
            String idString = hit.getUri().substring(indexOfStartOfId);
            Long id = Long.parseLong(idString);
            result.put(id, hit.getHits());
        }
        // возвращаем результат (события, у которых не было просмотров, не попадут в результат)
        return result;
    }

    private void addDates(LocalDateTime rangeStart, LocalDateTime rangeEnd,
                          JPAQuery<Event> query, QEvent qEvent) {
        // если не указана ни одна дата
        if (rangeStart == null && rangeEnd == null) {
            query.where(qEvent.eventDate.after(LocalDateTime.now()));
            // если получены обе даты
        } else if (rangeStart != null && rangeEnd != null) {
            validateDates(rangeStart, rangeEnd);
            query.where(qEvent.eventDate.between(rangeStart, rangeEnd));
            // если есть только дата начала периода
        } else if (rangeStart != null) {
            query.where(qEvent.eventDate.after(rangeStart));
            // если есть только дата окончания периода
        } else {
            query.where(qEvent.eventDate.before(rangeEnd));
        }
    }

    private void validateDates(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart.isAfter(rangeEnd)) {
            throw new ValidateException("Дата начала периода не должна быть позже даты окончания периода," +
                    "за который нужно искать события");
        }
    }

    private void addTextCategoriesAndPaid(String text, List<Long> categories, Boolean paid,
                                          JPAQuery<Event> query, QEvent qEvent) {
        if (text != null) {
            text = text.toLowerCase();
            query.where(qEvent.annotation.toLowerCase().contains(text)
                    .or(qEvent.description.toLowerCase().contains(text)));
        }
        if (categories != null) {
            query.where(qEvent.category.id.in(categories));
        }
        if (paid != null) {
            query.where(qEvent.paid.eq(paid));
        }
    }

}
