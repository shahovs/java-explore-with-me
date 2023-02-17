package ru.practicum.mainservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainservice.dto.ParticipationRequestDto;
import ru.practicum.mainservice.exception.ObjectNotFoundException;
import ru.practicum.mainservice.mapper.ParticipationRequestMapper;
import ru.practicum.mainservice.model.Event;
import ru.practicum.mainservice.model.ParticipationRequest;
import ru.practicum.mainservice.model.ParticipationRequestStatus;
import ru.practicum.mainservice.model.User;
import ru.practicum.mainservice.repository.EventRepository;
import ru.practicum.mainservice.repository.ParticipationRequestRepository;
import ru.practicum.mainservice.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationRequestServicePrivateImpl {

    private final ParticipationRequestRepository partRequestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ParticipationRequestMapper partRequestMapper;

    @Transactional
    public ParticipationRequestDto savePartRequest(Long requesterId, Long eventId) {
        ParticipationRequest participationRequest = createParticipationRequest(requesterId, eventId);
        participationRequest.setCreated(LocalDateTime.now());
        ParticipationRequest savedPartRequest = partRequestRepository.save(participationRequest);
        return partRequestMapper.toDto(savedPartRequest);
    }

    private ParticipationRequest createParticipationRequest(Long requesterId, Long eventId) {
        User requester = userRepository.findById(requesterId).orElseThrow(
                () -> new ObjectNotFoundException("Пользователь не найден"));
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new ObjectNotFoundException("Событие не найдено"));
        ParticipationRequest participationRequest = new ParticipationRequest();
        participationRequest.setRequester(requester);
        participationRequest.setEvent(event);
        participationRequest.setStatus(ParticipationRequestStatus.PENDING);
        return participationRequest;
    }

}
