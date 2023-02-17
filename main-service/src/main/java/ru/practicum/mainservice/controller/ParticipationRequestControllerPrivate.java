package ru.practicum.mainservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.dto.ParticipationRequestDto;
import ru.practicum.mainservice.service.impl.ParticipationRequestServicePrivateImpl;

@RestController
@RequestMapping(path = "/users/{requesterId}/requests")
@Slf4j
@RequiredArgsConstructor
@Validated
public class ParticipationRequestControllerPrivate {

    private final ParticipationRequestServicePrivateImpl partRequestServicePrivate;

    @PostMapping
    ResponseEntity<ParticipationRequestDto> savePartRequest(@PathVariable Long requesterId,
                                                            @RequestParam(required = false) Long userId,
                                                            @RequestParam Long eventId) {
        log.info("\n\nПолучен запрос к эндпоинту: POST /users/{requesterId}/requests\n" +
                "requesterId: {}, userId: {}, eventId: {}\n", requesterId, userId, eventId);
//        if (!Objects.equals(requesterId, userId)) {
//            throw new IllegalArgumentException("Ошибка запроса. " +
//                    "@PathVariable Long requesterId != @RequestParam Long userId");
//        }
        ParticipationRequestDto result = partRequestServicePrivate.savePartRequest(requesterId, eventId);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

}
