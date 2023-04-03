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
import ru.practicum.mainservice.model.EventState;
import ru.practicum.mainservice.service.impl.EventServiceAdminImpl;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(path = "/admin/events")
@Slf4j
@RequiredArgsConstructor
@Validated
public class EventControllerAdmin {

    private final EventServiceAdminImpl eventServiceAdmin;

    @GetMapping
    ResponseEntity<List<EventFullDto>> getEvents(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) List<EventState> states,
            @RequestParam(required = false) LocalDateTime rangeStart,
            @RequestParam(required = false) LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        log.info("\n\nПолучен запрос к эндпоинту: GET admin/events\nusers={}, categories={}, states={}\n" +
                        "rangeStart={}, rangeEnd={}\nfrom={}, size={}",
                users, categories, states, rangeStart, rangeEnd, from, size);
        List<EventFullDto> result = eventServiceAdmin.getEvents(users, categories, states, rangeStart, rangeEnd,
                from, size);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PatchMapping(path = "/{eventId}")
    ResponseEntity<EventFullDto> changeEvent(@PathVariable Long eventId,
                                           @Validated({Update.class}) @RequestBody
                                           EventUpdateAdminRequestDto eventUpdateAdminRequestDto) {
        log.info("\n\nПолучен запрос к эндпоинту: PATCH /admin/events/{}\n\n" +
                "Создан объект из тела запроса:\n'{}'", eventId, eventUpdateAdminRequestDto);
        EventFullDto result = eventServiceAdmin.changeEvent(eventId, eventUpdateAdminRequestDto);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}
