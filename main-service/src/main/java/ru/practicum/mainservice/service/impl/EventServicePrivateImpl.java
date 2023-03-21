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
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new ObjectNotFoundException("Событие не найдено"));
        if (!Objects.equals(initiatorId, event.getInitiator().getId())) {
            throw new ValidateException("Событие может запрашивать только создавший его пользователь");
        }
        Long confirmedRequests = event.getRequests().stream()
                .filter(request -> Objects.equals(request.getStatus(), ParticipationRequestStatus.CONFIRMED))
                .count();
        // добавляем количество просмотров события (из статистики)
        Long views = eventServicePublic.getViewsOfOneEvent(eventId);
        EventFullDto eventFullDto = eventMapper.toEventFullDto(event, confirmedRequests, views);
        return eventFullDto;
    }

    public List<EventShortDto> getEventsByUser(Long initiatorId, int from, int size) {
        // подготавливаем переменные для формирования запроса
        QEvent qEvent = QEvent.event;
        JPAQueryFactory factory = new JPAQueryFactory(entityManager);

        List<Event> events = factory
                .selectFrom(qEvent)
                .where(qEvent.initiator.id.eq(initiatorId))
                .offset(from)
                .limit(size)
                .fetch();

        List<EventShortDto> eventDtos = eventServicePublic.getEventShortDtos(events, false);
        return eventDtos;
    }

    @Transactional
    public EventFullDto saveEvent(Long initiatorId, EventNewDto eventNewDto) {
        if (eventNewDto.getEventDate().isBefore(LocalDateTime.now().plus(2, ChronoUnit.HOURS))) {
            throw new ValidateException("Событие не может быть раньше, чем через два часа от текущего момента");
        }
        User initiator = userRepository.findById(initiatorId).orElseThrow(
                () -> new ObjectNotFoundException("Инициатор события не найден"));
        Event event = eventMapper.toEntity(eventNewDto, initiator);
        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);
        // А можно ли как-то добавить в базу новую сущность Event,
        // не запрашивая из базы User и Category, а передав только их id (которые к нам пришли вместе с dto)?
        // Так чтобы не писать запрос вручную, конечно.
        eventRepository.save(event);
        return eventMapper.toEventFullDto(event);
    }

    @Transactional
    public EventFullDto changeEvent(Long initiatorId, Long eventId, EventUpdatePrivateRequestDto updateRequestDto) {
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new ObjectNotFoundException("Событие не найдено"));
        validateEventAndDto(initiatorId, updateRequestDto, event);
        changeStateAction(updateRequestDto, event);
        changeCategory(updateRequestDto, event);
        changeUsualFields(updateRequestDto, event);
        eventRepository.save(event);
        // заполнять в dto поля confirmedRequests и views не требуется,
        // так как изменять событие можно только до его публикации (EventState == PENDING или CANCELED)
        EventFullDto result = eventMapper.toEventFullDto(event);
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
        List<ParticipationRequest> requests = event.getRequests();
        List<ParticipationRequestDto> resultList = participationRequestMapper.toDtos(requests);
        return resultList;
    }

    @Transactional
    public EventRequestStatusUpdateResultDto changeStatusOfRequestsOfEvent(
            Long initiatorId, Long eventId, EventRequestStatusUpdateRequestDto updateRequestDto) {

        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new ObjectNotFoundException("Событие не найдено"));

        // если лимит заявок == 0 или отключена пре-модерация заявок, то подтверждение (/отклонение) заявок не требуется
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            return null;
        }

        List<ParticipationRequest> requests = event.getRequests();
        validateRequests(requests);

        switch (updateRequestDto.getStatus()) {
            case CONFIRMED:
                confirmRequests(requests, event);
                break;
            case REJECTED:
                requests = requests.stream()
                        .peek(request -> request.setStatus(ParticipationRequestStatus.REJECTED))
                        .collect(Collectors.toList());
                break;
            default:
                throw new ValidateException("Ошибка. Статус должен быть CONFIRMED или REJECTED");
        }

        partRequestRepository.saveAll(requests);

        Map<Boolean, List<ParticipationRequestDto>> requestDtos = requests.stream()
                .map(participationRequestMapper::toDto)
                .collect(Collectors.partitioningBy(
                        request -> Objects.equals(request.getStatus(), ParticipationRequestStatus.CONFIRMED)));

        return new EventRequestStatusUpdateResultDto(requestDtos.get(true), requestDtos.get(false));
    }

    private static void validateRequests(List<ParticipationRequest> requests) {
        // статус можно изменить только у заявок, находящихся в состоянии ожидания
        boolean notPending = requests.stream()
                .anyMatch(request ->
                        !Objects.equals(request.getStatus(), ParticipationRequestStatus.PENDING));
        if (notPending) {
            throw new ValidateException("Не все заявки находятся в состоянии ожидания");
        }
    }

    private void confirmRequests(List<ParticipationRequest> requests, Event event) {
        // нельзя подтвердить заявку, если уже достигнут лимит по заявкам на данное событие
        Integer requestCount = partRequestRepository.countAllByEventAndStatus(event, ParticipationRequestStatus.CONFIRMED);
        int participantLimit = event.getParticipantLimit();
        // тесты требуют учитывать все поданные заявки, а не только те, которые уже подтвердили
        // (причем даже ранее отклонненные заявки будут учитываться в расчете, что явно не верно)
//        Integer requestCount = partRequestRepository.countAllByEvent(event);
        if (participantLimit <= requestCount) {
            throw new LimitException("Достигнут лимит одобренных заявок");
        }

        // далее подтверждаем заявки
        // (если при этом лимит будет исчерпан, то все неподтверждённые заявки необходимо отклонить)
        int currentLimit = participantLimit - requestCount;
        for (ParticipationRequest request : requests) {
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
