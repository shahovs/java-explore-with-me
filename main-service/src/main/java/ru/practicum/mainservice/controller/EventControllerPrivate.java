package ru.practicum.mainservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.Create;
import ru.practicum.mainservice.dto.EventFullDto;
import ru.practicum.mainservice.dto.EventNewDto;
import ru.practicum.mainservice.service.impl.EventServicePrivateImpl;

@RestController
@RequestMapping(path = "/users/{initiatorId}/events")
@Slf4j
@RequiredArgsConstructor
@Validated
public class EventControllerPrivate {

    private final EventServicePrivateImpl eventServicePrivate;

    @PostMapping
    ResponseEntity<EventFullDto> saveEvent(@PathVariable Long initiatorId,
                                           @Validated({Create.class}) @RequestBody EventNewDto eventNewDto) {
        log.info("\n\nПолучен запрос к эндпоинту: POST /users/{}/events\n\n" +
                "Создан объект из тела запроса:\n'{}'", initiatorId, eventNewDto);
        EventFullDto result = eventServicePrivate.saveEvent(initiatorId, eventNewDto);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

}
