package ru.practicum.mainservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.Create;
import ru.practicum.mainservice.dto.*;
import ru.practicum.mainservice.service.impl.EventServicePrivateImpl;
import ru.practicum.mainservice.service.impl.EventServicePublicImpl;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping(path = "/events")
@Slf4j
@RequiredArgsConstructor
@Validated
public class EventControllerPublic {

    private final EventServicePublicImpl eventServicePublic;

    @GetMapping(path = "{eventId}")
    ResponseEntity<EventFullDto> getEventById(@PathVariable Long eventId, HttpServletRequest request) {
        log.info("\n\nПолучен запрос к эндпоинту: GET events/{}\nclient ip: {}", eventId, request.getRemoteAddr());
        EventFullDto result = eventServicePublic.getEventById(eventId, request.getRemoteAddr());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping
    ResponseEntity<List<EventShortDto>> getPublishedEvents(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) LocalDateTime rangeStart,
            @RequestParam(required = false) LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(required = false) String sort, //Available values : EVENT_DATE, VIEWS
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        log.info("\n\nПолучен запрос к эндпоинту: GET events/\ntext={}\ncategories={}\npaid={}\nrangeStart={}\n" +
                "rangeEnd={}\nonlyAvailable={}, sort={}, from={}, size={}\nclient ip: {}",
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size, request.getRemoteAddr());
        List<EventShortDto> result = eventServicePublic.getPublishedEvents(text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, sort, from, size, request.getRemoteAddr());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}
