package ru.practicum.mainservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.Update;
import ru.practicum.mainservice.dto.EventFullDto;
import ru.practicum.mainservice.dto.EventUpdateAdminRequestDto;
import ru.practicum.mainservice.service.impl.EventServiceAdminImpl;

@RestController
@RequestMapping(path = "/admin/events")
@Slf4j
@RequiredArgsConstructor
@Validated
public class EventControllerAdmin {

    private final EventServiceAdminImpl eventServiceAdmin;

    @PatchMapping(path = "/{eventId}")
    ResponseEntity<EventFullDto> changeEvent(@PathVariable Long eventId,
                                           @Validated({Update.class}) @RequestBody
                                           EventUpdateAdminRequestDto eventUpdateAdminRequestDto) {
        log.info("\n\nПолучен запрос к эндпоинту: PATCH /admin/events\n\n" +
                "Создан объект из тела запроса:\n'{}'", eventUpdateAdminRequestDto);
        EventFullDto result = eventServiceAdmin.changeEvent(eventId, eventUpdateAdminRequestDto);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}
