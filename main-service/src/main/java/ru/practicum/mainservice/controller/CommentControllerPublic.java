package ru.practicum.mainservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.dto.CommentDto;
import ru.practicum.mainservice.service.impl.CommentServicePublicImpl;

import java.util.List;

@RestController
@RequestMapping(path = "/comments")
@Slf4j
@RequiredArgsConstructor
@Validated
public class CommentControllerPublic {

    private final CommentServicePublicImpl commentServicePublic;

    @GetMapping(path = "/{commentId}")
    ResponseEntity<CommentDto> getCommentById(@PathVariable Long commentId) {
        log.info("\n\nПолучен запрос к эндпоинту: GET comments/{}\n", commentId);
        CommentDto result = commentServicePublic.getCommentById(commentId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping(path = "event/{eventId}")
    ResponseEntity<List<CommentDto>> getCommentsByEvent(@PathVariable Long eventId,
                                                        @RequestParam(defaultValue = "0") int from,
                                                        @RequestParam(defaultValue = "10") int size) {
        log.info("\n\nПолучен запрос к эндпоинту: GET comments/event/{}\n", eventId);
        int fromPage = from / size;
        Pageable pageable = PageRequest.of(fromPage, size);
        List<CommentDto> result = commentServicePublic.getCommentsByEvent(eventId, pageable);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}
