package ru.practicum.mainservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.Create;
import ru.practicum.mainservice.Update;
import ru.practicum.mainservice.dto.CommentDto;
import ru.practicum.mainservice.service.impl.CommentServicePrivateImpl;

@RestController
@RequestMapping(path = "/users/{commentator_id}/comments")
@Slf4j
@RequiredArgsConstructor
@Validated
public class CommentControllerPrivate {

    private final CommentServicePrivateImpl commentServicePrivate;

    @PostMapping
    ResponseEntity<CommentDto> saveComment(@PathVariable Long commentator_id,
                                           @RequestParam Long eventId,
                                           @Validated({Create.class}) @RequestBody CommentDto commentDto) {
        log.info("\n\nПолучен запрос к эндпоинту: POST /users/{}/comments, eventId = {}\n" +
                "Создан объект из тела запроса:\n'{}'", commentator_id, eventId, commentDto);
        CommentDto result = commentServicePrivate.saveComment(commentator_id, eventId, commentDto);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @PatchMapping(path = "/{commentId}")
    ResponseEntity<CommentDto> patchComment(@PathVariable Long commentator_id,
                                            @PathVariable Long commentId,
                                            @Validated({Update.class}) @RequestBody CommentDto commentDto) {
        log.info("\n\nПолучен запрос к эндпоинту: PATCH /users/{}/comments/{}\n" +
                "Создан объект из тела запроса:\n'{}'", commentator_id, commentId, commentDto);
        CommentDto result = commentServicePrivate.patchComment(commentator_id, commentId, commentDto);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping(path = "/{commentId}")
    ResponseEntity<Void> deleteComment(@PathVariable Long commentator_id,
                                       @PathVariable Long commentId) {
        log.info("\n\nПолучен запрос к эндпоинту: DELETE /users/{}/comments/{}\n", commentator_id, commentId);
        commentServicePrivate.deleteComment(commentator_id, commentId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
