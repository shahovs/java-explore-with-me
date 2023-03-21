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
import ru.practicum.mainservice.model.EventState;
import ru.practicum.mainservice.model.ParticipationRequestStatus;
import ru.practicum.mainservice.model.QEvent;
import ru.practicum.mainservice.model.QParticipationRequest;
import ru.practicum.mainservice.repository.EventRepository;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServicePublicImpl {

    private final EventRepository eventRepository;
    private final StatsClient statsClient;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final EntityManager entityManager;

    public EventFullDto getEventById(Long eventId, String ip) {
        EventFullDto eventFullDto = findEventFullDto(eventId);
        validateEvent(eventFullDto);
        // получаем количество просмотров события (из статистики)
        Long views = getViewsOfOneEvent(eventId);
        eventFullDto.setViews(views);
        // отправляем в статистику информацию о просмотре события
        statsClient.postStatMonolit("emw", "/events/" + eventId, ip,
                LocalDateTime.now().format(DATE_TIME_FORMATTER));
        return eventFullDto;
    }

    EventFullDto findEventFullDto(Long eventId) {
        List<EventFullDto> resultList = eventRepository.findEventsFullDto(Collections.singletonList(eventId));
        if (resultList.size() == 1) {
            return resultList.get(0);
        }
        throw new ObjectNotFoundException("Событие не найдено или недоступно");
    }

    private void validateEvent(EventFullDto eventFullDto) {
        if (!eventFullDto.getState().equals(EventState.PUBLISHED)) {
            throw new ValidateException("Событие должно быть опубликовано");
        }
    }

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
        QParticipationRequest qParticipationRequest = QParticipationRequest.participationRequest;
        JPAQueryFactory factory = new JPAQueryFactory(entityManager);

        // todo есть идея вернуться к первончальному замыслу, когда мы получаем обертку (класс), внутри
        // которой два поля - сущность Event и количество запросов
        // затем уже эту обертку обрабатываем (переделываем Event in dto, добавляем запросы и просмотры
        // в этом случае не придется многократно перечислять все поля в select для контруктора dto
        // и потом повторять их при преобразовании tuple in dto
        // Отдельный момент. Вместо обертки можно использовать проекцию (интерфейс без создания класса)
        // см. 4.1 https://www.baeldung.com/jpa-queries-custom-result-with-aggregation-functions
        // (класс, реализующий интерфейс в этом случае спринг сделает сам)


        // основная часть запроса (select, from, join, основные условия - PUBLISHED, CONFIRMED)
        JPAQuery<Tuple> query = factory
                .select(qEvent.id, qEvent.title, qEvent.annotation,
                        qEvent.category.id, qEvent.category.name,
                        qEvent.eventDate, qEvent.paid,
                        qEvent.initiator.id, qEvent.initiator.name,
                        qParticipationRequest.count())
                .from(qEvent)
                .leftJoin(qParticipationRequest).on(qParticipationRequest.event.eq(qEvent))
                .where(qEvent.state.eq(EventState.PUBLISHED))
                .where(qParticipationRequest.status.eq(ParticipationRequestStatus.CONFIRMED)
                        .or(qParticipationRequest.isNull()));
        // добавляем условия, полученные в запросе (даты, текст, категории, платность участия)
        addDates(rangeStart, rangeEnd, query, qEvent);
        addTextCategoriesAndPaid(text, categories, paid, query, qEvent);
        // группируем (по событиям - у одного события может быть несколько строк - по кол-ву запросов на участие)
        query.groupBy(qEvent, qEvent.category.name, qEvent.initiator.name);
        // если нужно, то оставляем только те события, у которых не исчерпан лимит запросов на участие
        if (onlyAvailable) {
            query.having(qParticipationRequest.count().lt(qEvent.participantLimit));
        }
        // сортируем (вынести в отдельный метод по возможности)
        if (sort == null || sort.equals("EVENT_DATE")) {
            query.orderBy(qEvent.eventDate.asc());
        } else if (!sort.equals("VIEWS")) {
            // если sort == VIEWS, то сортировку будем делать в конце
            throw new IllegalArgumentException("Способ сортировки должен быть EVENT_DATE, VIEWS или null");
        }
        // применяем from, size
        query.offset(from).limit(size); // query.restrict() ???
        // делаем запрос
        List<Tuple> tuples = query.fetch();
        // преобразуем результат в список dto и одновременно получаем список id для запроса статистики
        List<Long> eventIds = new ArrayList<>();
        List<EventShortDto> eventDtos = new ArrayList<>();
        for (Tuple tuple : tuples) {
            eventDtos.add(new EventShortDto(
                    tuple.get(qEvent.id), tuple.get(qEvent.title), tuple.get(qEvent.annotation),
                    tuple.get(qEvent.category.id), tuple.get(qEvent.category.name),
                    tuple.get(qEvent.eventDate), tuple.get(qEvent.paid),
                    tuple.get(qEvent.initiator.id), tuple.get(qEvent.initiator.name),
                    tuple.get(qParticipationRequest.count())
            ));
            eventIds.add(tuple.get(qEvent.id));
        }
        // запрашиваем просмотры в сервисе статистики (будут получены только те события, у которые были просмотры)
        Map<Long, Long> viewsMap = getViews(eventIds);
        // добавляем просмотры в dto
        for (EventShortDto dto : eventDtos) {
            Long views = viewsMap.get(dto.getId());
            dto.setViews(Objects.requireNonNullElse(views, 0L));
        }
        // если нужно, делаем сортировку по просмотрам (но тогда ломается пагинация)
        // а по другому никак - только добавлять views в таблицу events, но по заданию должно быть так:
        // "сортировка событий должна быть по кол-ву просмотров, которое будет запрашиваться в сервисе статистики";
        // либо придется делать сначала запрос в events для получения всех подходящих ids (без пагинации),
        // потом делать запрос в статистику для получения всех views
        // (метод клиента статистики не преполагает сортировки и пагинации)
        // потом вручную делать сортировку и пагинацию и второй запрос в events для получения отобранных событий
        // то есть получится три запроса вместо двух, причем два из них без ограничения (size) результатов
        if (sort != null && sort.equals("VIEWS")) {
            eventDtos.sort(Comparator.comparing(EventShortDto::getViews).reversed());
        }

        // отправляем в статистику данные о сделанном запросе
        statsClient.postStat("emw", "/events", ip, LocalDateTime.now().format(DATE_TIME_FORMATTER));
        return eventDtos;
    }

    // todo сделать универсальный метод, который обрабатывает как один id (лист-одиночку), так и неск.
    // но еще раз обдумать, есть ли смысл - скорее всего, нет
    // (но сначала избавиться от дат в виде строк (поменять метод клиента))
    // тогда можно будет убрать второй метод getViewsOfOneEvent
    // для этого делаем if (list.size == 0) {return new HashMap}, else if(list.size==1){
    // String[] uri = {"/events/" + eventId}; }
    // else { код для случая нескольких id }
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
                          JPAQuery<Tuple> query, QEvent qEvent) {
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
                                          JPAQuery<Tuple> query, QEvent qEvent) {
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
