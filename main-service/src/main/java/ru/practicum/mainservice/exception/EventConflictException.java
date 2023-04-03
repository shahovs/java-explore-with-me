package ru.practicum.mainservice.exception;

public class EventConflictException extends RuntimeException {
    public EventConflictException(String message) {
        super(message);
    }
}