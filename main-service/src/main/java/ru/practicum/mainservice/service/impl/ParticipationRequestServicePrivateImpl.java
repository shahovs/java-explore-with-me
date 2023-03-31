package ru.practicum.mainservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainservice.dto.ParticipationRequestDto;
import ru.practicum.mainservice.exception.ObjectNotFoundException;
import ru.practicum.mainservice.exception.ValidateException;
import ru.practicum.mainservice.mapper.ParticipationRequestMapper;
import ru.practicum.mainservice.model.*;
import ru.practicum.mainservice.repository.EventRepository;
import ru.practicum.mainservice.repository.ParticipationRequestRepository;
import ru.practicum.mainservice.repository.UserRepository;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ParticipationRequestServicePrivateImpl {

    private final ParticipationRequestRepository partRequestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ParticipationRequestMapper partRequestMapper;

    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getPartRequestsByUser(Long requesterId) {
        User requester = userRepository.findById(requesterId).orElseThrow(
                () -> new ObjectNotFoundException("Пользователь не найден"));
        return partRequestRepository.findAllByRequester(requester);
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

    @Transactional
    public ParticipationRequestDto savePartRequest(Long requesterId, Long eventId) {
        User requester = userRepository.findById(requesterId).orElseThrow(
                () -> new ObjectNotFoundException("Пользователь не найден"));
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new ObjectNotFoundException("Событие не найдено"));
        validatePartRequest(requesterId, event);
        ParticipationRequest participationRequest = new ParticipationRequest(requester, event);
        partRequestRepository.save(participationRequest);
        return partRequestMapper.toDto(participationRequest);
    }

    private void validatePartRequest(Long requesterId, Event event) {
        if (Objects.equals(requesterId, event.getInitiator().getId())) {
            throw new ValidateException("Ошибка. Инициатор события не может делать запрос на участие в своём событии");
        }
        if (!Objects.equals(event.getState(), EventState.PUBLISHED)) {
            throw new ValidateException("Ошибка. Нельзя оставлять запросы на участие в неопубликованных событиях");
        }
        // если у события отсутствует ограничение на количество участников (==0), то проверки завершены
        if (Objects.equals(event.getParticipantLimit(), 0)) {
            return;
        }
        // иначе проверяем, что ограничение на участников не превышено
        Integer confirmedRequests = partRequestRepository.countAllByEventAndStatus(event,
                ParticipationRequestStatus.CONFIRMED);
        if ((event.getParticipantLimit() - confirmedRequests) <= 0) {
            throw new ValidateException("Ошибка добавления запроса на участие. У события уже заполнен лимит участия");
        }
    }

}
