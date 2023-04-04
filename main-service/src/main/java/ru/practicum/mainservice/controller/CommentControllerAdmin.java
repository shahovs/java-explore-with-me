package ru.practicum.mainservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.Update;
import ru.practicum.mainservice.dto.CommentDto;
import ru.practicum.mainservice.service.impl.CommentServiceAdminImpl;

@RestController
@RequestMapping(path = "/admin/comments")
@Slf4j
@RequiredArgsConstructor
@Validated
public class CommentControllerAdmin {

    private final CommentServiceAdminImpl commentServiceAdmin;

    @PatchMapping(path = "/{commentId}")
    ResponseEntity<CommentDto> patchComment(@PathVariable Long commentId,
                                            @Validated({Update.class}) @RequestBody CommentDto commentDto) {
        log.info("\n\nПолучен запрос к эндпоинту: PATCH /admin/comments/{}\n" +
                "Создан объект из тела запроса:\n'{}'",  commentId, commentDto);
        CommentDto result = commentServiceAdmin.patchComment(commentId, commentDto);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping(path = "/{commentId}")
    ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        log.info("\n\nПолучен запрос к эндпоинту: DELETE /admin/comments/{}\n", commentId);
        commentServiceAdmin.deleteComment(commentId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
