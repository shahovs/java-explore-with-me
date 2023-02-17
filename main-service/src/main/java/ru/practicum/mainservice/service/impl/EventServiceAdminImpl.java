package ru.practicum.mainservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainservice.dto.EventFullDto;
import ru.practicum.mainservice.dto.EventStateAction;
import ru.practicum.mainservice.dto.EventUpdateAdminRequestDto;
import ru.practicum.mainservice.exception.EventConflictException;
import ru.practicum.mainservice.exception.ObjectNotFoundException;
import ru.practicum.mainservice.mapper.EventMapper;
import ru.practicum.mainservice.model.Category;
import ru.practicum.mainservice.model.Event;
import ru.practicum.mainservice.model.EventState;
import ru.practicum.mainservice.repository.CategoryRepository;
import ru.practicum.mainservice.repository.EventRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceAdminImpl {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;

    @Transactional
    public EventFullDto changeEvent(Long eventId, EventUpdateAdminRequestDto eventDto) {
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new ObjectNotFoundException("Событие не найдено"));
        changeEventDateAndStateAction(eventDto, event);
        changeCategory(eventDto, event);
        changeUsualFields(eventDto, event);
        Event changedEvent = eventRepository.save(event);
        return eventMapper.toEventFullDto(changedEvent);
    }

    // менять состояние мероприятия (публиковать) можно только после изменения даты мероприятия,
    // так как возможность публикации мероприятия зависит от его даты (поэтому метод разделять нельзя)
    private void changeEventDateAndStateAction(EventUpdateAdminRequestDto eventDto, Event event) {
        if (eventDto.getEventDate() != null) {
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
        }
    }

}
