package ru.practicum.mainservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainservice.dto.EventFullDto;
import ru.practicum.mainservice.dto.EventNewDto;
import ru.practicum.mainservice.exception.ObjectNotFoundException;
import ru.practicum.mainservice.mapper.EventMapper;
import ru.practicum.mainservice.model.Event;
import ru.practicum.mainservice.model.EventState;
import ru.practicum.mainservice.model.User;
import ru.practicum.mainservice.repository.EventRepository;
import ru.practicum.mainservice.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServicePrivateImpl {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    //    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;

    @Transactional
    public EventFullDto saveEvent(Long initiatorId, EventNewDto eventNewDto) {
//        validateUser(initiatorId);
        User initiator = userRepository.findById(initiatorId).orElseThrow(
                () -> new ObjectNotFoundException("Инициатор события не найден"));
//        Category category = categoryRepository.findById(eventNewDto.getCategory()).orElseThrow(
//                () ->  new ObjectNotFoundException("Категория для события не найдена"));
        Event event = eventMapper.toEntity(eventNewDto, initiator);
        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);
        System.out.println("\nevent: " + event);
        Event savedEvent = eventRepository.save(event);
        System.out.println("\nsavedEvent:\n" + savedEvent);
        EventFullDto eventFullDto = eventMapper.toEventFullDto(savedEvent);
        System.out.println("\neventFullDto:\n" + eventFullDto);
        return eventFullDto;
    }

//    private void validateUser(Long initiatorId) {
//        if (!userRepository.existsById(initiatorId)) {
//            throw new ObjectNotFoundException("Инициатор события не найден");
//        }
//    }

}
