package ru.practicum.mainservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.mainservice.dto.ErrorResponse;
import ru.practicum.mainservice.exception.*;

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

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleConflictException(final ConflictException e) {
        log.info("409 {}", e.getMessage(), e);
        final HttpStatus status = HttpStatus.CONFLICT;
        ErrorResponse errorResponse = new ErrorResponse(e, status, "Ошибка. ConflictException");
        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleLimitException(final LimitException e) {
        log.info("409 {}", e.getMessage(), e);
        final HttpStatus status = HttpStatus.CONFLICT;
        ErrorResponse errorResponse = new ErrorResponse(e, status, "Ошибка. LimitException");
        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        log.info("400 {}", e.getMessage(), e);
        final HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorResponse errorResponse = new ErrorResponse(e, status, "Ошибка. MethodArgumentNotValidException");
        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleValidateException(final ValidateException e) {
        log.info("409 {}", e.getMessage(), e);
        final HttpStatus status = HttpStatus.CONFLICT;
        ErrorResponse errorResponse = new ErrorResponse(e, status, "Ошибка. ValidateException");
        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(final DataIntegrityViolationException e) {
        log.info("409 {}", e.getMessage(), e);
        final HttpStatus status = HttpStatus.CONFLICT;
        ErrorResponse errorResponse = new ErrorResponse(e, status, "Ошибка. DataIntegrityViolationException");
        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
            final MissingServletRequestParameterException e) {
        log.info("400 {}", e.getMessage(), e);
        final HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorResponse errorResponse = new ErrorResponse(e, status, "Ошибка. MissingServletRequestParameterException");
        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleThrowable(final Throwable e) {
        log.info("500 {}", e.getMessage(), e);
        final HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorResponse errorResponse = new ErrorResponse(e, status, "Ошибка. Throwable");
        return new ResponseEntity<>(errorResponse, status);
    }

}
