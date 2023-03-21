package ru.practicum.mainservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.Create;
import ru.practicum.mainservice.dto.*;
import ru.practicum.mainservice.service.impl.EventServicePrivateImpl;

import java.util.List;

@RestController
@RequestMapping(path = "/users/{initiatorId}/events")
@Slf4j
@RequiredArgsConstructor
@Validated
public class EventControllerPrivate {

    private final EventServicePrivateImpl eventServicePrivate;

    @GetMapping(path = "/{eventId}")
    ResponseEntity<EventFullDto> getEventById(@PathVariable Long initiatorId, @PathVariable Long eventId) {
        log.info("\n\nПолучен запрос к эндпоинту: GET /users/{}/events/{}", initiatorId, eventId);
        EventFullDto result = eventServicePrivate.getEventById(initiatorId, eventId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping
    ResponseEntity<List<EventShortDto>> getEventsByUser(@PathVariable Long initiatorId,
                                                        @RequestParam(defaultValue = "0") int from,
                                                        @RequestParam(defaultValue = "10") int size) {
        log.info("\n\nПолучен запрос к эндпоинту: GET /users/{}/events", initiatorId);
        List<EventShortDto> result = eventServicePrivate.getEventsByUser(initiatorId, from, size);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping
    ResponseEntity<EventFullDto> saveEvent(@PathVariable Long initiatorId,
                                           @Validated({Create.class}) @RequestBody EventNewDto eventNewDto) {
        log.info("\n\nПолучен запрос к эндпоинту: POST /users/{}/events\n" +
                "Создан объект из тела запроса:\n'{}'", initiatorId, eventNewDto);
        EventFullDto result = eventServicePrivate.saveEvent(initiatorId, eventNewDto);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @PatchMapping(path = "/{eventId}")
    ResponseEntity<EventFullDto> changeEvent(@PathVariable Long initiatorId, @PathVariable Long eventId,
                                             @RequestBody EventUpdatePrivateRequestDto updateRequestDto) {
        log.info("\n\nПолучен запрос к эндпоинту: PATCH /users/{}/events/{}/\n" +
                "Создан объект из тела запроса:\n'{}'", initiatorId, eventId, updateRequestDto);
        EventFullDto result = eventServicePrivate.changeEvent(initiatorId, eventId, updateRequestDto);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping(path = "/{eventId}/requests")
    ResponseEntity<List<ParticipationRequestDto>> getRequestsOfEvent(@PathVariable Long initiatorId, @PathVariable Long eventId) {
        log.info("\n\nПолучен запрос к эндпоинту: GET /users/{}/events/{}/requests", initiatorId, eventId);
        List<ParticipationRequestDto> result = eventServicePrivate.getRequestsOfEvent(initiatorId, eventId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PatchMapping(path = "/{eventId}/requests")
    ResponseEntity<EventRequestStatusUpdateResultDto> changeStatusOfRequestsOfEvent(
            @PathVariable Long initiatorId,
            @PathVariable Long eventId,
            @RequestBody EventRequestStatusUpdateRequestDto updateRequestDto) {
        log.info("\n\nПолучен запрос к эндпоинту: PATCH /users/{}/events/{}/requests\n" +
                "Создан объект из тела запроса:\n'{}'", initiatorId, eventId, updateRequestDto);
        EventRequestStatusUpdateResultDto result = eventServicePrivate.changeStatusOfRequestsOfEvent(
                initiatorId, eventId, updateRequestDto);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}
