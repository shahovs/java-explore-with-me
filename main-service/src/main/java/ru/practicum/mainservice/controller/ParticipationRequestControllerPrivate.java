package ru.practicum.mainservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.dto.ParticipationRequestDto;
import ru.practicum.mainservice.service.impl.ParticipationRequestServicePrivateImpl;

import java.util.List;

@RestController
@RequestMapping(path = "/users/{requesterId}/requests")
@Slf4j
@RequiredArgsConstructor
@Validated
public class ParticipationRequestControllerPrivate {

    private final ParticipationRequestServicePrivateImpl partRequestServicePrivate;

    @GetMapping
    ResponseEntity<List<ParticipationRequestDto>> getPartRequestsByUser(@PathVariable Long requesterId) {
        log.info("\n\nПолучен запрос к эндпоинту: GET /users/{requesterId}/requests, requesterId: {}", requesterId);
        List<ParticipationRequestDto> result = partRequestServicePrivate.getPartRequestsByUser(requesterId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping
    ResponseEntity<ParticipationRequestDto> savePartRequest(@PathVariable Long requesterId,
                                                            @RequestParam Long eventId) {
        log.info("\n\nПолучен запрос к эндпоинту: POST /users/{}/requests/{}\n", requesterId, eventId);
        ParticipationRequestDto result = partRequestServicePrivate.savePartRequest(requesterId, eventId);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @PatchMapping(path = "/{requestId}/cancel")
    ResponseEntity<ParticipationRequestDto> cancelPartRequest(@PathVariable Long requesterId,
                                                              @PathVariable Long requestId) {
        log.info("\n\nПолучен запрос к эндпоинту: PATCH /users/{}/requests/{}/cancel", requesterId, requestId);
        ParticipationRequestDto result = partRequestServicePrivate.canselPartRequest(requesterId, requestId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}
