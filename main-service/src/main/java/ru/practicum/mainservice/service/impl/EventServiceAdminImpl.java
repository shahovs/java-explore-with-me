package ru.practicum.mainservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainservice.dto.EventFullDto;
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
    @PersistenceContext
    private final EntityManager entityManager;

    public List<EventFullDto> getEvents(List<Long> users, List<Long> categories, List<EventState> states,
                                        LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                        int from, int size) {
        // подготавливаем данные
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<EventFullDto> cq = cb.createQuery(EventFullDto.class);
        // from join
        Root<Event> root = cq.from(Event.class);
        Join<Event, ParticipationRequest> requests = root.join("requests", JoinType.LEFT);

        // указываем что брать (select)
        cq.multiselect(
                root.get("id"), root.get("title"), root.get("annotation"), root.get("description"),
                root.get("category").get("id"), root.get("category").get("name"),
                root.get("eventDate"), root.get("location"), root.get("paid"), root.get("participantLimit"),
                root.get("requestModeration"), root.get("createdOn"), root.get("publishedOn"),
                root.get("initiator").get("id"), root.get("initiator").get("name"),
                root.get("state"), cb.count(requests));

        // создаем предикаты для where
        List<Predicate> predicates = new ArrayList<>();
        // если массив пустой (не null, а ноль элементов), то как тогда ?
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

        cq.where(predicates.toArray(new Predicate[0]));

        // группируем, создаем запрос в репозиторий, добавляем from, size и отправляем запрос
        cq.groupBy(root, root.get("category").get("name"), root.get("initiator").get("name"));
        TypedQuery<EventFullDto> query = entityManager.createQuery(cq);
        query.setFirstResult(from).setMaxResults(size);
        List<EventFullDto> resultList = query.getResultList();

        // получаем id опубликованных событий, запрашиваем просмотры в статистике, добавляем их к dto событий
        List<Long> ids = resultList.stream()
                .filter(eventFullDto -> eventFullDto.getState().equals(EventState.PUBLISHED))
                .map(EventFullDto::getId)
                .collect(Collectors.toList());
        Map<Long, Long> viewsMap = eventServicePublic.getViews(ids);
        for (EventFullDto dto : resultList) {
            Long views = viewsMap.get(dto.getId());
            dto.setViews(Objects.requireNonNullElse(views, 0L));
        }
        return resultList;
    }

    // todo наверное, нужно добавить просмотры и подтвержденные запросы на участие к измененному событию
    //  (если оно опубликовано)
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
            if (eventDate.isBefore(LocalDateTime.now().plus(1, ChronoUnit.HOURS))) {
                throw new ValidateException("Дата начала изменяемого события должна быть в будущем " +
                        "и не менее, чем через час от даты публикации события");
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
                LocalDateTime nowPlusOneHour = LocalDateTime.now().plus(1, ChronoUnit.HOURS);
                if (event.getEventDate().isBefore(nowPlusOneHour))
                    throw new EventConflictException("" +
                            "Дата начала изменяемого события должна быть не ранее чем за час от даты публикации");
                if (event.getState().equals(EventState.PENDING)) {
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                } else {
                    throw new EventConflictException("" +
                            "Событие можно публиковать, только если оно в состоянии ожидания публикации");
                }
                break;
            case REJECT_EVENT:
                if (!event.getState().equals(EventState.PUBLISHED)) {
                    event.setState(EventState.CANCELED);
                } else {
                    throw new EventConflictException("" +
                            "Событие можно отклонить, только если оно еще не опубликовано");
                }
                break;
            default:
                throw new ValidateException("Состояние изменяемого события должно быть PUBLISH_EVENT, REJECT_EVENT или null");
        }
    }

}
