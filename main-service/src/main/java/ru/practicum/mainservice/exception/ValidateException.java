package ru.practicum.mainservice.exception;

public class ValidateException extends RuntimeException {
    public ValidateException(String message) {
        super(message);
    }
}
