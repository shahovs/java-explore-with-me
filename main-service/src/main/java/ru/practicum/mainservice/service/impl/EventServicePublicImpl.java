package ru.practicum.mainservice.service.impl;

import com.querydsl.core.Tuple;
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
import ru.practicum.mainservice.repository.EventRepository;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServicePublicImpl {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    private final StatsClient statsClient;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final EntityManager entityManager;

    public EventFullDto getEventById(Long eventId, String ip) {
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new ObjectNotFoundException("Событие не найдено"));
        if (!Objects.equals(event.getState(), EventState.PUBLISHED)) {
            throw new ValidateException("Событие должно быть опубликовано");
        }
        Long confirmedRequests = event.getRequests().stream()
                .filter(request -> Objects.equals(request.getStatus(), ParticipationRequestStatus.CONFIRMED))
                .count();
        // получаем количество просмотров события (из статистики)
        Long views = getViewsOfOneEvent(eventId);
        // отправляем в статистику информацию о просмотре события
        statsClient.postStatMonolit("emw", "/events/" + eventId, ip,
                LocalDateTime.now().format(DATE_TIME_FORMATTER));
        EventFullDto eventFullDto = eventMapper.toEventFullDto(event, confirmedRequests, views);
        return eventFullDto;
    }

//    EventFullDto findEventFullDto(Long eventId) {
//        List<EventFullDto> resultList = eventRepository.findEventsFullDto(Collections.singletonList(eventId));
//        if (resultList.size() == 1) {
//            return resultList.get(0);
//        }
//        throw new ObjectNotFoundException("Событие не найдено или недоступно");
//    }

//    private void validateEvent(EventFullDto eventFullDto) {
//        if (!eventFullDto.getState().equals(EventState.PUBLISHED)) {
//            throw new ValidateException("Событие должно быть опубликовано");
//        }
//    }

    // запрашиваем в статистике количество просмотров
    Long getViewsOfOneEvent(Long eventId) {
        // подготавливаем данные
        String[] uri = {"/events/" + eventId};
        String start = LocalDateTime.of(2023, 1, 1, 0, 0).format(DATE_TIME_FORMATTER);
        String end = LocalDateTime.now().format(DATE_TIME_FORMATTER);

        // запрашиваем статистику
        HitShortWithHitsDtoResponse[] statArray = statsClient.getStatArray(start, end, uri, false);
//        ResponseEntity<Object> statObject = statsClient.getStatObject(start, end, uri, false);
//        List<Map> list = (List<Map>) statObject.getBody();
//        if (list.size() == 1) {
//            Map<String, Object> map = list.get(0);
//            Object views = map.get("hits");
//            Integer result = (Integer) views;
//            return (long) result;
//        }
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
//        QParticipationRequest qParticipationRequest = QParticipationRequest.participationRequest;
//        QUser qUser = QUser.user;
//        QCategory qCategory = QCategory.category;
        JPAQueryFactory factory = new JPAQueryFactory(entityManager);

        // Отдельный момент. Вместо обертки можно использовать проекцию (интерфейс без создания класса)
        // см. 4.1 https://www.baeldung.com/jpa-queries-custom-result-with-aggregation-functions
        // (класс, реализующий интерфейс в этом случае спринг сделает сам)
// todo как сделать, чтобы получался один запрос в базу вместо 4 отдельных (Event, User, Category, PartRequest)?
        JPAQuery<Event> query = factory.selectFrom(qEvent)
//                .join(qUser).on(qUser.id.eq(qEvent.initiator.id))
//                .join(qCategory).on(qCategory.id.eq(qEvent.category.id))
//                .leftJoin(qParticipationRequest).on(qParticipationRequest.event.eq(qEvent))
                .where(qEvent.state.eq(EventState.PUBLISHED));

        // добавляем условия, полученные в запросе (даты, текст, категории, платность участия)
        addDates(rangeStart, rangeEnd, query, qEvent);
        addTextCategoriesAndPaid(text, categories, paid, query, qEvent);

        if (sort != null && sort.equals("EVENT_DATE")) {
            query.orderBy(qEvent.eventDate.asc());
        }

        // применяем from, size
        query.offset(from).limit(size); // query.restrict() ???
        // делаем запрос
        List<Event> events = query.fetch();

        List<EventShortDto> eventDtos = getEventShortDtos(events, onlyAvailable);

        // если нужно, делаем сортировку по просмотрам (но тогда ломается пагинация)
        // а по другому никак - только добавлять views в таблицу events, но по заданию должно быть так:
        // "сортировка событий должна быть по кол-ву просмотров, которое будет запрашиваться в сервисе статистики";
        // либо придется делать сначала запрос в events для получения всех подходящих ids (без пагинации),
        // потом делать запрос в статистику для получения всех views
        // (метод клиента статистики не преполагает сортировки и пагинации)
        // потом вручную делать сортировку и пагинацию и второй запрос в events для получения отобранных событий
        // то есть получится три запроса вместо двух, причем два из них без ограничения (size) результатов
        // (аналогичная проблема и с onlyAvailable)
        if (sort != null && sort.equals("VIEWS")) {
            eventDtos.sort(Comparator.comparing(EventShortDto::getViews).reversed());
        }

        // отправляем в статистику данные о сделанном запросе
        statsClient.postStat("emw", "/events", ip, LocalDateTime.now().format(DATE_TIME_FORMATTER));
        return eventDtos;
    }

    List<EventShortDto> getEventShortDtos(List<Event> events, Boolean onlyAvailable) {
        List<EventShortDto> eventDtos = new ArrayList<>();

        // запрашиваем просмотры в сервисе статистики (будут получены только те события, у которых были просмотры)
        Map<Long, Long> viewsMap = getViewsMap(events);

        for (Event event : events) {
            long views = viewsMap.getOrDefault(event.getId(), 0L);
            int participantLimit = event.getParticipantLimit();
            long confirmedRequests = getConfirmedRequests(event, participantLimit);
            // добавляем в dtos либо все события (!onlyAvailable), либо те, у которых нет лимита участия,
            // либо те, у которых не исчерпан лимит участия
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
        Map<Long, Long> viewsMap = getViews(eventIds);
        return viewsMap;
    }

    Map<Long, Long> getViews(List<Long> eventIds) {
        if (eventIds == null || eventIds.size() == 0) {
            return null;
        }
        // подготавливаем данные
        String[] uri = new String[eventIds.size()];
        final String EVENTS = "/events/";
        for (int i = 0; i < eventIds.size(); i++) {
            uri[i] = EVENTS + eventIds.get(i);
        }
        String start = LocalDateTime.of(2023, 1, 1, 0, 0).format(DATE_TIME_FORMATTER);
        String end = LocalDateTime.now().format(DATE_TIME_FORMATTER);

        // запрашиваем статистику
        HitShortWithHitsDtoResponse[] statArray = statsClient.getStatArray(start, end, uri, false);

        // обрабатываем результат запроса
        int startOfId = EVENTS.length();
        Map<Long, Long> result = new HashMap<>();
        for (HitShortWithHitsDtoResponse hit : statArray) {
            String idString = hit.getUri().substring(startOfId);
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
