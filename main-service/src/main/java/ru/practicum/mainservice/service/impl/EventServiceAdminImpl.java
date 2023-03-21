package ru.practicum.mainservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainservice.dto.EventFullDto;
import ru.practicum.mainservice.dto.EventShortDto;
import ru.practicum.mainservice.dto.EventStateAction;
import ru.practicum.mainservice.dto.EventUpdateAdminRequestDto;
import ru.practicum.mainservice.exception.ConflictException;
import ru.practicum.mainservice.exception.EventConflictException;
import ru.practicum.mainservice.exception.ObjectNotFoundException;
import ru.practicum.mainservice.exception.ValidateException;
import ru.practicum.mainservice.mapper.EventMapper;
import ru.practicum.mainservice.model.*;
import ru.practicum.mainservice.repository.CategoryRepository;
import ru.practicum.mainservice.repository.EventRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceAdminImpl {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final EventServicePublicImpl eventServicePublic;
    private final EventMapper eventMapper;
    @PersistenceContext // todo Будет ли работать без этой аннотации и что она означает?
    private final EntityManager entityManager;

    public List<EventFullDto> getEvents(List<Long> users, List<Long> categories, List<EventState> states,
                                        LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                        int from, int size) {
        // подготавливаем данные
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> cq = cb.createQuery(Event.class);
        Root<Event> root = cq.from(Event.class);
        cq.select(root);

        // создаем предикаты для where
        List<Predicate> predicates = new ArrayList<>();
        // todo если массив пустой (не null, а ноль элементов), то как тогда ?
        if (users != null) {
            predicates.add(root.get("initiator").in(users));
        }
        if (categories != null) {
            predicates.add(root.get("category").in(categories));
        }
        if (states != null) {
            predicates.add(root.get("state").in(states));
        }

        if (rangeStart != null && rangeEnd != null) {
            predicates.add(cb.between(root.get("eventDate"), rangeStart, rangeEnd));
        } else if (rangeStart != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
        } else if (rangeEnd != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
        }

        cq.where(predicates.toArray(new Predicate[0])); // todo (new Predicate[predicates.size()]) ???

        TypedQuery<Event> query = entityManager.createQuery(cq);
        query.setFirstResult(from).setMaxResults(size);
        List<Event> events = query.getResultList();

        List<EventFullDto> eventDtos = new ArrayList<>();

        // запрашиваем просмотры в сервисе статистики (будут получены только те события, у которых были просмотры)
        Map<Long, Long> viewsMap = eventServicePublic.getViewsMap(events);

        for (Event event : events) {
            long views = viewsMap.getOrDefault(event.getId(), 0L);
            int participantLimit = event.getParticipantLimit();
            long confirmedRequests = EventServicePublicImpl.getConfirmedRequests(event, participantLimit);
            eventDtos.add(eventMapper.toEventFullDto(event, confirmedRequests, views));
        }
        return eventDtos;
    }

    @Transactional
    public EventFullDto changeEvent(Long eventId, EventUpdateAdminRequestDto eventDto) {
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new ObjectNotFoundException("Событие не найдено"));
        changeEventDateAndStateAction(eventDto, event);
        changeCategory(eventDto, event);
        changeUsualFields(eventDto, event);
        Event changedEvent = eventRepository.save(event);
        EventFullDto result = eventMapper.toEventFullDto(changedEvent);
        return result;
    }

    // менять состояние мероприятия (публиковать) можно только после изменения даты мероприятия,
    // так как возможность публикации мероприятия зависит от его даты (поэтому метод разделять нельзя)
    private void changeEventDateAndStateAction(EventUpdateAdminRequestDto eventDto, Event event) {
        if (eventDto.getEventDate() != null) {
            LocalDateTime eventDate = eventDto.getEventDate();
            // todo проверить, что проверка нужна именно здесь (для тестов), а не там, где меняется состояние
            if (eventDate.isBefore(LocalDateTime.now().plus(1, ChronoUnit.HOURS))) {
                throw new ValidateException("Дата начала изменяемого события должна быть " +
                        "не менее, чем через час от даты публикации события");
            }
            event.setEventDate(eventDto.getEventDate());
        }
        changeStateAction(eventDto, event);
    }

    private void changeCategory(EventUpdateAdminRequestDto eventDto, Event event) {
        if (eventDto.getCategory() == null) {
            return;
        }
        Category category = categoryRepository.findById(eventDto.getCategory()).orElseThrow(
                () -> new ObjectNotFoundException("Категория не найдена"));
        event.setCategory(category);
    }

    private void changeUsualFields(EventUpdateAdminRequestDto eventDto, Event event) {
        if (eventDto.getTitle() != null) {
            event.setTitle(eventDto.getTitle());
        }
        if (eventDto.getAnnotation() != null) {
            event.setAnnotation(eventDto.getAnnotation());
        }
        if (eventDto.getDescription() != null) {
            event.setDescription(eventDto.getDescription());
        }
        if (eventDto.getLocation() != null) {
            event.setLocation(eventDto.getLocation());
        }
        if (eventDto.getPaid() != null) {
            event.setPaid(eventDto.getPaid());
        }
        if (eventDto.getParticipantLimit() != null) {
            event.setParticipantLimit(eventDto.getParticipantLimit());
        }
        if (eventDto.getRequestModeration() != null) {
            event.setRequestModeration(eventDto.getRequestModeration());
        }
    }

    private void changeStateAction(EventUpdateAdminRequestDto eventDto, Event event) {
        EventStateAction stateAction = eventDto.getStateAction();
        if (stateAction == null) {
            return;
        }
        switch (stateAction) {
            case PUBLISH_EVENT:
//                LocalDateTime nowPlusOneHour = LocalDateTime.now().plus(1, ChronoUnit.HOURS);
//                if (event.getEventDate().isBefore(nowPlusOneHour))
//                    throw new EventConflictException("" +
//                            "Дата начала изменяемого события должна быть не ранее чем за час от даты публикации");
                if (Objects.equals(event.getState(), EventState.PENDING)) {
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                } else {
                    throw new EventConflictException("" +
                            "Событие можно публиковать, только если оно в состоянии ожидания публикации");
                }
                break;
            case REJECT_EVENT:
                if (!Objects.equals(event.getState(), EventState.PUBLISHED)) {
                    event.setState(EventState.CANCELED);
                } else {
                    throw new EventConflictException("" +
                            "Событие можно отклонить, только если оно еще не опубликовано");
                }
                break;
            default:
                throw new ValidateException("Состояние изменяемого события должно быть " +
                        "PUBLISH_EVENT, REJECT_EVENT или null");
        }
    }

}
