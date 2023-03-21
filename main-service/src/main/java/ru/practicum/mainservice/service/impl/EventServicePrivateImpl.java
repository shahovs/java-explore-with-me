package ru.practicum.mainservice.service.impl;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stat.client.StatsClient;
import ru.practicum.mainservice.dto.*;
import ru.practicum.mainservice.exception.LimitException;
import ru.practicum.mainservice.exception.ObjectNotFoundException;
import ru.practicum.mainservice.exception.ValidateException;
import ru.practicum.mainservice.mapper.EventMapper;
import ru.practicum.mainservice.mapper.ParticipationRequestMapper;
import ru.practicum.mainservice.model.*;
import ru.practicum.mainservice.repository.CategoryRepository;
import ru.practicum.mainservice.repository.EventRepository;
import ru.practicum.mainservice.repository.ParticipationRequestRepository;
import ru.practicum.mainservice.repository.UserRepository;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServicePrivateImpl {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ParticipationRequestRepository requestRepository;
    private final ParticipationRequestRepository partRequestRepository;
    private final EntityManager entityManager;
    private final EventMapper eventMapper;
    private final ParticipationRequestMapper participationRequestMapper;
    private final StatsClient statsClient;
    private final EventServicePublicImpl eventServicePublic;

    public EventFullDto getEventById(Long initiatorId, Long eventId) {
        EventFullDto eventFullDto = eventServicePublic.findEventFullDto(eventId);

        if (!initiatorId.equals(eventFullDto.getInitiator().getId())) {
            throw new ValidateException("Событие может запрашивать только создавший его пользователь");
        }

        // добавляем количество просмотров события (из статистики)
        Long views = eventServicePublic.getViewsOfOneEvent(eventId);
        eventFullDto.setViews(views);
        return eventFullDto;
    }

    public List<EventShortDto> getEventsByUser(Long initiatorId, int from, int size) {
        // подготавливаем переменные для формирования запроса
        QEvent qEvent = QEvent.event;
        QParticipationRequest qParticipationRequest = QParticipationRequest.participationRequest;
        JPAQueryFactory factory = new JPAQueryFactory(entityManager);

        // основная часть запроса (select, from, join, основные условия - initiatorId, CONFIRMED)
        JPAQuery<Tuple> query = factory
                .select(qEvent.id, qEvent.title, qEvent.annotation,
                        qEvent.category.id, qEvent.category.name,
                        qEvent.eventDate, qEvent.paid,
                        qEvent.initiator.id, qEvent.initiator.name,
                        qParticipationRequest.count())
                .from(qEvent)
                .leftJoin(qParticipationRequest).on(qParticipationRequest.event.eq(qEvent))
                .where(qEvent.initiator.id.eq(initiatorId))
                .where(qParticipationRequest.status.eq(ParticipationRequestStatus.CONFIRMED)
                        .or(qParticipationRequest.isNull()));
        // группируем (по событиям - у одного события может быть несколько строк - по кол-ву запросов на участие)
        query.groupBy(qEvent, qEvent.category.name, qEvent.initiator.name);
        // применяем from, size
        query.offset(from).limit(size);
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
        Map<Long, Long> viewsMap = eventServicePublic.getViews(eventIds);
        // добавляем просмотры в dto
        for (EventShortDto dto : eventDtos) {
            Long views = viewsMap.get(dto.getId());
            dto.setViews(Objects.requireNonNullElse(views, 0L));
        }
        return eventDtos;
    }

    @Transactional
    public EventFullDto saveEvent(Long initiatorId, EventNewDto eventNewDto) {
        if (eventNewDto.getEventDate().isBefore(LocalDateTime.now().plus(2, ChronoUnit.HOURS))) {
            throw new ValidateException("Событие не может быть раньше, чем через два часа от текущего момента");
        }
        User initiator = userRepository.findById(initiatorId).orElseThrow(
                () -> new ObjectNotFoundException("Инициатор события не найден"));
//        Category category = categoryRepository.findById(eventNewDto.getCategory()).orElseThrow(
//                () ->  new ObjectNotFoundException("Категория для события не найдена"));
        Event event = eventMapper.toEntity(eventNewDto, initiator);
        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);
        Event savedEvent = eventRepository.save(event);
        return eventMapper.toEventFullDto(savedEvent);
    }

    @Transactional
    public EventFullDto changeEvent(Long initiatorId, Long eventId, EventUpdatePrivateRequestDto updateRequestDto) {
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new ObjectNotFoundException("Событие не найдено"));
        validateEventAndDto(initiatorId, updateRequestDto, event);
        changeStateAction(updateRequestDto, event);
        changeCategory(updateRequestDto, event);
        changeUsualFields(updateRequestDto, event);
        Event changedEvent = eventRepository.save(event);
        // заполнять в dto поля confirmedRequests и views не требуется,
        // так как изменять событие можно только до его публикации (EventState == PENDING или CANCELED)
        EventFullDto result = eventMapper.toEventFullDto(changedEvent);
        return result;
    }

    private static void validateEventAndDto(Long initiatorId, EventUpdatePrivateRequestDto updateRequestDto, Event event) {
        if (!Objects.equals(initiatorId, event.getInitiator().getId())) {
            throw new ValidateException("Событие может изменить только его инициатор");
        }
        if (!Objects.equals(event.getState(), EventState.PENDING)
                && !Objects.equals(event.getState(), EventState.CANCELED)) {
            throw new ValidateException("Изменить можно только события в состоянии ожидания модерации или отмененные");
        }
        if (updateRequestDto.getEventDate() != null
                && updateRequestDto.getEventDate().isBefore(LocalDateTime.now().plus(2, ChronoUnit.HOURS))) {
            throw new ValidateException("Событие не может быть раньше, чем через два часа от текущего момента");
        }
    }

    private void changeStateAction(EventUpdatePrivateRequestDto updateRequestDto, Event event) {
        EventStateAction stateAction = updateRequestDto.getStateAction();
        if (stateAction == null) {
            return;
        }
        switch (stateAction) {
            case SEND_TO_REVIEW:
                event.setState(EventState.PENDING);
                break;
            case CANCEL_REVIEW:
                event.setState(EventState.CANCELED);
                break;
            default:
                throw new ValidateException("Состояние изменяемого события должно быть SEND_TO_REVIEW, CANCEL_REVIEW или null");
        }
    }

    private void changeCategory(EventUpdatePrivateRequestDto updateRequestDto, Event event) {
        if (updateRequestDto.getCategory() == null) {
            return;
        }
        Category category = categoryRepository.findById(updateRequestDto.getCategory()).orElseThrow(
                () -> new ObjectNotFoundException("Категория не найдена"));
        event.setCategory(category);
    }

    private void changeUsualFields(EventUpdatePrivateRequestDto updateRequestDto, Event event) {
        if (updateRequestDto.getTitle() != null) {
            event.setTitle(updateRequestDto.getTitle());
        }
        if (updateRequestDto.getAnnotation() != null) {
            event.setAnnotation(updateRequestDto.getAnnotation());
        }
        if (updateRequestDto.getDescription() != null) {
            event.setDescription(updateRequestDto.getDescription());
        }
        if (updateRequestDto.getEventDate() != null) {
            event.setEventDate(updateRequestDto.getEventDate());
        }
        if (updateRequestDto.getLocation() != null) {
            event.setLocation(updateRequestDto.getLocation());
        }
        if (updateRequestDto.getPaid() != null) {
            event.setPaid(updateRequestDto.getPaid());
        }
        if (updateRequestDto.getParticipantLimit() != null) {
            event.setParticipantLimit(updateRequestDto.getParticipantLimit());
        }
        if (updateRequestDto.getRequestModeration() != null) {
            event.setRequestModeration(updateRequestDto.getRequestModeration());
        }
    }

    public List<ParticipationRequestDto> getRequestsOfEvent(Long initiatorId, Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new ObjectNotFoundException("Событие не найдено"));
        if (!Objects.equals(initiatorId, event.getInitiator().getId())) {
            throw new ValidateException("Запросы на участие в событии может получить только инициатор события");
        }
        List<ParticipationRequest> allByEvent = requestRepository.findAllByEvent(event);
        List<ParticipationRequestDto> resultList = participationRequestMapper.toDtos(allByEvent);
        return resultList;
    }

    @Transactional
    public EventRequestStatusUpdateResultDto changeStatusOfRequestsOfEvent(
            Long initiatorId, Long eventId, EventRequestStatusUpdateRequestDto updateRequestDto) {

        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new ObjectNotFoundException("Событие не найдено"));
        int participantLimit = event.getParticipantLimit();

        // если для события лимит заявок равен 0  отключена пре-модерация заявок,
        // то подтверждение (/отклонение) заявок не требуется
        if (!event.getRequestModeration() || participantLimit == 0) {
            return null;
        }

        // статус можно изменить только у заявок, находящихся в состоянии ожидания
        List<ParticipationRequest> allRequestsByIds = partRequestRepository.findAllById(updateRequestDto.getRequestIds());
        boolean notPending = allRequestsByIds.stream()
                .anyMatch(participationRequest ->
                        !participationRequest.getStatus().equals(ParticipationRequestStatus.PENDING));
        if (notPending) {
            throw new ValidateException("Не все заявки находятся в состоянии ожидания");
        }

        // если передан статус REJECTED, то отклоняем заявки
        if (updateRequestDto.getStatus().equals(ParticipationRequestStatus.REJECTED)) {
            allRequestsByIds = allRequestsByIds.stream()
                    .peek(participationRequest -> participationRequest.setStatus(ParticipationRequestStatus.REJECTED))
                    .collect(Collectors.toList());
            // если же передан статус CONFIRMED, то одобряем заявки
        } else if (updateRequestDto.getStatus().equals(ParticipationRequestStatus.CONFIRMED)) {
            confirmRequests(allRequestsByIds, event);
        } else {
            throw new ValidateException("Ошибка. Статус должен быть CONFIRMED или REJECTED");
        }

        // сохраняем результат (заявки с измененными статусами)
        List<ParticipationRequest> savedPartRequests = partRequestRepository.saveAll(allRequestsByIds);

        // преобразовываем в dto
        Map<Boolean, List<ParticipationRequestDto>> requestDtos = savedPartRequests.stream()
                .map(participationRequestMapper::toDto)
                .collect(Collectors.partitioningBy(
                        request -> request.getStatus().equals(ParticipationRequestStatus.CONFIRMED)));

        return new EventRequestStatusUpdateResultDto(requestDtos.get(true), requestDtos.get(false));
    }

    private void confirmRequests(List<ParticipationRequest> allRequestsByIds, Event event) {
        int participantLimit = event.getParticipantLimit();

        // нельзя подтвердить заявку, если уже достигнут лимит по заявкам на данное событие
        Integer requestCount = partRequestRepository.countAllByEventAndStatus(event, ParticipationRequestStatus.CONFIRMED);
        // тесты требуют учитывать все поданные заявки, а не только те, которые уже подтвердили
        // (причем даже ранее отклонненные заявки будут учитываться в расчете, что явно не верно)
//        Integer requestCount = partRequestRepository.countAllByEvent(event);
        if (participantLimit <= requestCount) {
            throw new LimitException("Достигнут лимит одобренных заявок");
        }

        // далее подтверждаем заявки
        // (если при этом лимит будет исчерпан, то все неподтверждённые заявки необходимо отклонить)
        int currentLimit = participantLimit - requestCount;
        for (ParticipationRequest request : allRequestsByIds) {
            if (currentLimit > 0) {
                request.setStatus(ParticipationRequestStatus.CONFIRMED);
            } else {
                request.setStatus(ParticipationRequestStatus.REJECTED);
            }
            --currentLimit;
        }
    }


    // запрашиваем в статистике количество просмотров
//    private Long getViews(Long eventId) {
//        // подготавливаем данные
//        String[] uri = {"/events/" + eventId};
//
//        // разобраться, почему не работает без форматтера (с настройками в AppConfig)
////        String start = LocalDateTime.of(2023, 1, 1, 0, 0).toString();
////        String end = LocalDateTime.now().toString();
//
//        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//        String start = LocalDateTime.of(2023, 1, 1, 0, 0).format(dateTimeFormatter);
//        String end = LocalDateTime.now().format(dateTimeFormatter);
//
//        // запрашиваем статистику
//        // первый вариант метода клиента
//        ResponseEntity<Object> stat = statsClient.getStat(start, end, uri, false);
//        List<HitShortWithHitsDtoResponse> resultList = (List<HitShortWithHitsDtoResponse>) stat.getBody();
//        // второй вариант метода клиента
//        List<HitShortWithHitsDtoResponse> statList = statsClient.getStatList(start, end, uri, false);
//        // третий вариант метода клиента
//        HitShortWithHitsDtoResponse[] statArray = statsClient.getStatArray(start, end, uri, false);
//
//        if (resultList.size() == 1) {
//            HitShortWithHitsDtoResponse hitShortWithHitsDtoResponse = resultList.get(0);
//            return hitShortWithHitsDtoResponse.getHits();
//        }
//        if (statList.size() == 1) {
//            HitShortWithHitsDtoResponse hitShortWithHitsDtoResponse = statList.get(0);
//            return hitShortWithHitsDtoResponse.getHits();
//        }
//        if (statArray.length == 1) {
//            HitShortWithHitsDtoResponse hitShortWithHitsDtoResponse = statArray[0];
//            return hitShortWithHitsDtoResponse.getHits();
//        }
//        return 0L;
//    }

}
