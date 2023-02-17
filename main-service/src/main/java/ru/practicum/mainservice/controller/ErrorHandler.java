package ru.practicum.mainservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.mainservice.ErrorResponse;
import ru.practicum.mainservice.exception.DuplicateException;
import ru.practicum.mainservice.exception.EventConflictException;
import ru.practicum.mainservice.exception.ObjectNotFoundException;

@RestControllerAdvice("ru.practicum.mainservice")
@Slf4j
public class ErrorHandler {

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleObjectDidntFoundException(final ObjectNotFoundException e) {
        log.info("404 {}", e.getMessage(), e);
        final HttpStatus status = HttpStatus.NOT_FOUND;
        ErrorResponse errorResponse = new ErrorResponse(e, status, "Ошибка. ObjectNotFoundException");
        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleDuplicateException(final DuplicateException e) {
        log.info("409 {}", e.getMessage(), e);
        final HttpStatus status = HttpStatus.CONFLICT;
        ErrorResponse errorResponse = new ErrorResponse(e, status, "Ошибка. DuplicateException");
        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleEventConflictException(final EventConflictException e) {
        log.info("409 {}", e.getMessage(), e);
        final HttpStatus status = HttpStatus.CONFLICT;
        ErrorResponse errorResponse = new ErrorResponse(e, status, "Ошибка. EventConflictException");
        return new ResponseEntity<>(errorResponse, status);
    }

}
