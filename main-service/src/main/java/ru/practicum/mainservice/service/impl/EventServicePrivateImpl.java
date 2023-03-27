package ru.practicum.mainservice.service.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServicePrivateImpl {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ParticipationRequestRepository partRequestRepository;
    private final EntityManager entityManager;
    private final EventMapper eventMapper;
    private final ParticipationRequestMapper participationRequestMapper;
    private final EventServicePublicImpl eventServicePublic;

    public EventFullDto getEventById(Long initiatorId, Long eventId) {
        Event event = getEventWithGraphAndValidate(initiatorId, eventId);
        Long confirmedRequests = event.getRequests().stream()
                .filter(request -> Objects.equals(request.getStatus(), ParticipationRequestStatus.CONFIRMED))
                .count();
        // добавляем количество просмотров события (из статистики)
        Long views = eventServicePublic.getViewsOfOneEvent(eventId);
        EventFullDto eventFullDto = eventMapper.toEventFullDto(event, confirmedRequests, views);
        return eventFullDto;
    }

    private Event getEventWithGraphAndValidate(Long initiatorId, Long eventId) {
        EntityGraph<?> entityGraph = entityManager.getEntityGraph("event-entity-graph");
        Map<String, Object> properties = new HashMap<>();
        properties.put("javax.persistence.fetchgraph", entityGraph);
        Event event = entityManager.find(Event.class, eventId, properties);
        validateEventAndInitiator(initiatorId, event);
        return event;
    }

    private static void validateEventAndInitiator(Long initiatorId, Event event) {
        if (event == null) {
            throw new ObjectNotFoundException("Событие не найдено");
        }
        if (!Objects.equals(initiatorId, event.getInitiator().getId())) {
            throw new ValidateException("Запрос может делать только пользователь, создавший событие (инициатор)");
        }
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
                .setHint("javax.persistence.fetchgraph", entityManager.getEntityGraph("event-entity-graph"))
                .fetch();

        List<EventShortDto> eventDtos = eventServicePublic.mapToEventShortDtos(events);
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
        // todo А можно ли как-то добавить в базу новую сущность Event,
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
                throw new ValidateException(
                        "Состояние изменяемого события должно быть SEND_TO_REVIEW, CANCEL_REVIEW или null");
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
        Event event = getEventWithGraphAndValidate(initiatorId, eventId);
        List<ParticipationRequest> requests = event.getRequests();
        List<ParticipationRequestDto> resultList = participationRequestMapper.toDtos(requests);
        return resultList;
    }

    @Transactional
    public EventRequestStatusUpdateResultDto changeStatusOfRequestsOfEvent(
            Long initiatorId, Long eventId, EventRequestStatusUpdateRequestDto updateRequestDto) {
        // получаем событие со всеми запросами на участие
        Event event = getEventWithGraphAndValidate(initiatorId, eventId);

        // если отключена пре-модерация заявок или лимит заявок == 0, то подтверждение (/отклонение) заявок не требуется
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            return null;
        }

        // получаем запросы на участие в событии, у которых нужно изменить статус
        List<ParticipationRequest> allRequestsOfEvent = event.getRequests();
        final List<Long> changingRequestIds = updateRequestDto.getRequestIds();
        List<ParticipationRequest> changingRequests = allRequestsOfEvent.stream()
                .filter(request -> changingRequestIds.contains(request.getId()))
                .collect(Collectors.toList());

        validateRequests(changingRequests);

        switch (updateRequestDto.getStatus()) {
            case CONFIRMED:
                confirmRequests(changingRequests, event);
                break;
            case REJECTED:
                changingRequests = changingRequests.stream()
                        .peek(request -> request.setStatus(ParticipationRequestStatus.REJECTED))
                        .collect(Collectors.toList());
                break;
            default:
                throw new ValidateException("Ошибка. Статус должен быть CONFIRMED или REJECTED");
        }

        partRequestRepository.saveAll(changingRequests);

        Map<Boolean, List<ParticipationRequestDto>> requestDtos = changingRequests.stream()
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
        int confirmedRequestCount = (int) event.getRequests().stream()
                .filter(request -> Objects.equals(request.getStatus(), ParticipationRequestStatus.CONFIRMED))
                .count();
        int participantLimit = event.getParticipantLimit();
        // нельзя подтвердить заявку, если уже достигнут лимит по заявкам на данное событие
        if (participantLimit <= confirmedRequestCount) {
            throw new LimitException("Достигнут лимит одобренных заявок");
        }

        // подтверждаем заявки (если при этом лимит будет исчерпан, то все неподтверждённые заявки необходимо отклонить)
        int currentLimit = participantLimit - confirmedRequestCount;
        for (ParticipationRequest request : requests) {
            if (currentLimit > 0) {
                request.setStatus(ParticipationRequestStatus.CONFIRMED);
            } else {
                request.setStatus(ParticipationRequestStatus.REJECTED);
            }
            --currentLimit;
        }
    }

}
