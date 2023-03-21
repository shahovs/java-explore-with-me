package ru.practicum.mainservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainservice.dto.EventWithConfirmedRequestsDto;
import ru.practicum.mainservice.dto.ParticipationRequestDto;
import ru.practicum.mainservice.exception.ConflictException;
import ru.practicum.mainservice.exception.ObjectNotFoundException;
import ru.practicum.mainservice.exception.ValidateException;
import ru.practicum.mainservice.mapper.ParticipationRequestMapper;
import ru.practicum.mainservice.model.*;
import ru.practicum.mainservice.repository.EventRepository;
import ru.practicum.mainservice.repository.ParticipationRequestRepository;
import ru.practicum.mainservice.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
//@Transactional(readOnly = true)
public class ParticipationRequestServicePrivateImpl {

    private final ParticipationRequestRepository partRequestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ParticipationRequestMapper partRequestMapper;

    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getPartRequestsByUser(Long requesterId) {
        User requester = userRepository.findById(requesterId).orElseThrow(
                () -> new ObjectNotFoundException("Пользователь не найден"));
        List<ParticipationRequest> allPartRequestsByRequester = partRequestRepository.findAllByRequester(requester);
        return partRequestMapper.toDtos(allPartRequestsByRequester);
    }

    //    @Transactional
    public ParticipationRequestDto savePartRequest(Long requesterId, Long eventId) {
        List<EventWithConfirmedRequestsDto> eventDto =
                eventRepository.findEventsOneQuery(Collections.singletonList(eventId));
        validatePartRequest(requesterId, eventDto);
        ParticipationRequest participationRequest = createParticipationRequest(requesterId, eventId);
        participationRequest.setCreated(LocalDateTime.now());
        ParticipationRequest savedPartRequest = partRequestRepository.save(participationRequest);
        return partRequestMapper.toDto(savedPartRequest);
    }

    private void validatePartRequest(Long requesterId, List<EventWithConfirmedRequestsDto> eventDto) {
        // todo сделать в репозитории метод для запроса с одним id и получения Optional
        if (eventDto.size() != 1) {
            throw new ObjectNotFoundException("Событие не найдено");
        }
        EventWithConfirmedRequestsDto dto = eventDto.get(0);
        Event event = dto.getEvent();
        if (requesterId.equals(event.getInitiator().getId())) {
            throw new ValidateException("Ошибка. Инициатор события не может делать запрос на участие в своём событии");
        }
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ValidateException("Ошибка. Нельзя оставлять запросы на участие в неопубликованных событиях");
        }
        // если у события отсутствует ограничение на количество участников (==0), то проверки завершены
        if (event.getParticipantLimit().equals(0)) {
            return;
        }
        // иначе проверяем, что ограничение на участников не превышено
        if (event.getParticipantLimit() - dto.getConfirmedRequests() <= 0) {
            throw new ValidateException("Ошибка добавления запроса на участие. У события уже заполнен лимит участия");
        }
    }

    @Transactional
    public ParticipationRequestDto canselPartRequest(Long requesterId, Long requestId) {
        ParticipationRequest participationRequest = partRequestRepository.findById(requestId).orElseThrow(
                () -> new ObjectNotFoundException("Запрос не найден или недоступен"));
        if (!requesterId.equals(participationRequest.getRequester().getId())) {
            throw new ValidateException("Отменить можно только свой запрос");
        }
        participationRequest.setStatus(ParticipationRequestStatus.CANCELED);
        partRequestRepository.save(participationRequest);
        return partRequestMapper.toDto(participationRequest);
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
